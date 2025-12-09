package no.nav.historisk.superhelt.sak

import jakarta.validation.Valid
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavUser
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.rest.SakCreateRequestDto
import no.nav.historisk.superhelt.sak.rest.SakUpdateRequestDto
import no.nav.historisk.superhelt.sak.rest.UtbetalingRequestDto
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class SakService(
    private val sakRepository: SakRepository,
    private val endringsloggService: EndringsloggService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.fnr)")
    @Transactional
    fun createSak(@Valid req: SakCreateRequestDto): Sak {

        val soknadsDato = req.soknadsDato ?: LocalDate.now()
        val saksbehandler= getCurrentNavUser()
        val sak =
            SakJpaEntity(
                type = req.type,
                fnr = req.fnr,
                tittel = req.tittel,
                status = SakStatus.UNDER_BEHANDLING,
                soknadsDato = soknadsDato,
                tildelingsAar = soknadsDato.year,
                saksbehandler = saksbehandler
            )
        val saved = sakRepository.save(sak)
        logger.info("Opprettet ny sak med saksnummer {}", saved.saksnummer)
        return saved
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun updateSak(saksnummer: Saksnummer, @Valid req: SakUpdateRequestDto): Sak {
        val sak = sakRepository.getSakEntityOrThrow(saksnummer)
        req.type?.let { sak.type = it }
        req.tittel?.let { sak.tittel = it }
        req.begrunnelse?.let { sak.begrunnelse = it }
        req.soknadsDato?.let { sak.soknadsDato = it }
        sak.tildelingsAar = req.tildelingsAar?.value
        req.vedtaksResultat?.let { sak.vedtaksResultat = it }
        sak.saksbehandler = getCurrentNavUser()
        logger.debug("Oppdaterer sak med saksnummer {}", saksnummer)
        return sakRepository.save(sak)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun updateUtbetaling(saksnummer: Saksnummer, req: UtbetalingRequestDto): Sak {
        val sak = sakRepository.getSakEntityOrThrow(saksnummer)

        when (req.utbetalingsType) {
            UtbetalingsType.BRUKER -> {
                sak.setOrUpdateUtbetaling(req.belop ?: 0)
                sak.forhandstilsagn = null
            }

            UtbetalingsType.FORHANDSTILSAGN -> {
                sak.utbetaling = null
                sak.setOrUpdateForhandsTilsagn(req.belop ?: 0)
            }

            UtbetalingsType.INGEN -> {
                sak.utbetaling = null
                sak.forhandstilsagn = null
            }
        }
        logger.info("Oppdaterer sak med saksnummer {}", saksnummer)
        return sakRepository.save(sak)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun gjenapneSak(sak: Sak, kommentar: String) {
        val saksnummer=sak.saksnummer
        val status = SakStatus.UNDER_BEHANDLING
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        if (status === sakEntity.status) {
            logger.debug("Sak {} status er allerede {}, ingen endring gjort.", saksnummer, status)
            return
        }
        sakEntity.status = status
        sakEntity.attestant = null

        sakRepository.save(sakEntity)
        logger.info("Sak {} endret status til {}", saksnummer, status)
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun sendTilAttestering(sak: Sak) {
        val saksnummer=sak.saksnummer
        val status = SakStatus.TIL_ATTESTERING

        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        if (status === sakEntity.status) {
            logger.debug("Sak {} status er allerede {}, ingen endring gjort.", saksnummer, status)
            return
        }
        sakEntity.status = status
        sakEntity.saksbehandler = getCurrentNavUser()
        sakEntity.attestant = null
        sakRepository.save(sakEntity)
        logger.info("Sak {} endret status til {}", saksnummer, status)

    }


    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun ferdigstill( sak: Sak) {
        val saksnummer=sak.saksnummer
        val sakEntity = sakRepository.getSakEntityOrThrow(saksnummer)
        val status = SakStatus.FERDIG
        if (status == sakEntity.status) {
            logger.debug("Sak {} status er allerede {}, ingen endring gjort.", saksnummer, status)
            return
        }
        sakEntity.status = status
        sakEntity.attestant = getCurrentNavUser()
        sakRepository.save(sakEntity)
        logger.info("Sak {} endret status til {}", saksnummer, status)
    }
}
