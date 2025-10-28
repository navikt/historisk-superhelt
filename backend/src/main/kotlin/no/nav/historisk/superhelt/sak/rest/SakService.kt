package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SakService(private val sakRepository: SakRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)


    @Transactional
    fun createSak(req: SakCreateRequestDto): Sak {
        val sakEntity = Sak(
            type = req.type,
            fnr = req.fnr,
            tittel = req.tittel,
            status = SakStatus.UNDER_BEHANDLING,
            soknadsDato = req.soknadsDato,
            saksbehandler = getCurrentNavIdent() ?: "ukjent"
        )
        return sakRepository.save(sakEntity)
    }

    @Transactional
    fun updateSak(saksNummer: Saksnummer, req: SakUpdateRequestDto): Sak {
        val sak = sakRepository.getSakOrThrow(saksNummer)
        val oppdatertSak = sak.copy(
            tittel = req.tittel ?: sak.tittel,
            begrunnelse = req.begrunnelse ?: sak.begrunnelse,
            type = req.type ?: sak.type,
            soknadsDato = req.soknadsDato ?: sak.soknadsDato,
            vedtak = req.vedtak ?: sak.vedtak,
            utbetaling = toUtbetaling(req.utbetalingsType, req.belop, sak),
            forhandstilsagn = toForhandstilsagn(req),
        )

        return sakRepository.save(oppdatertSak)
    }

    private fun toForhandstilsagn(req: SakUpdateRequestDto): Forhandstilsagn? {
        if (req.utbetalingsType == UtbetalingsType.FORHANDSTILSAGN) {
            return Forhandstilsagn()
        }
        return null
    }

    private fun toUtbetaling(type: UtbetalingsType?, belop: Double?, sak: Sak): Utbetaling? {
        if (type == UtbetalingsType.BRUKER) {
            return belop?.let {
                Utbetaling(
                    bruker = sak.fnr,
                    belop = it
                )
            }
        }
        return null

    }

}