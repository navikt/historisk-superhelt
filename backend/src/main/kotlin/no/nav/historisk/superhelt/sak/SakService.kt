package no.nav.historisk.superhelt.sak

import jakarta.validation.Valid
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.sak.db.SakJpaEntity
import no.nav.historisk.superhelt.sak.rest.SakCreateRequestDto
import no.nav.historisk.superhelt.sak.rest.SakUpdateRequestDto
import no.nav.historisk.superhelt.sak.rest.UtbetalingsType
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SakService(private val sakRepository: SakRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PreAuthorize("hasAuthority('WRITE') and @tilgangsmaskin.harTilgang(#req.fnr)")
    @Transactional
    fun createSak(@Valid req: SakCreateRequestDto): Sak {

        val sak = SakJpaEntity(
            type = req.type,
            fnr = req.fnr,
            tittel = req.tittel,
            status = SakStatus.UNDER_BEHANDLING,
            soknadsDato = req.soknadsDato,
            saksbehandler = getCurrentNavIdent() ?: "ukjent"
        )
        val saved = sakRepository.save(sak)
        logger.info("Opprettet ny sak med saksnummer {}", saved.saksnummer)
        return saved
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun updateSak(saksNummer: Saksnummer, @Valid req: SakUpdateRequestDto): Sak {
        val sak = sakRepository.getSakEntityOrThrow(saksNummer)
        req.type?.let { sak.type = it }
        req.tittel?.let { sak.tittel = it }
        req.begrunnelse?.let { sak.begrunnelse = it }
        req.soknadsDato?.let { sak.soknadsDato = it }
        req.vedtak?.let { sak.vedtak = it }
        //TODO validere utbetaling og kanskje flytte det ut i egen service
        req.utbetalingsType?.let {
            when (it) {
                UtbetalingsType.BRUKER -> {
                    sak.setOrUpdateUtbetaling(req.utbetaling)
                    sak.forhandstilsagn = null
                }

                UtbetalingsType.FORHANDSTILSAGN -> {
                    sak.utbetaling = null
                    sak.setOrUpdateForhandsTilsagn(req.forhandstilsagn)
                }

                UtbetalingsType.INGEN -> {
                    sak.utbetaling = null
                    sak.forhandstilsagn = null
                }
            }

        }
        logger.info("Oppdaterer sak med saksnummer {}", saksNummer)
        return sakRepository.save(sak)
    }


    @PreAuthorize("hasAuthority('WRITE')")
    @Transactional
    fun changeStatus(saksnummer: Saksnummer, status: SakStatus) {
        val sak = sakRepository.getSakEntityOrThrow(saksnummer)
        if (status === sak.status) {
            logger.debug("Sak {} status er allerede {}, ingen endring gjort.", saksnummer, status)
            return
        }
        sak.status = status
        sakRepository.save(sak)
        logger.debug("Sak {} endret statsu til {}", saksnummer, status)
    }



}