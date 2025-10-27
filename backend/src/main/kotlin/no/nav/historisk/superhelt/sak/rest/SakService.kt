package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.Saksnummer
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
            vedtak = req.vedtak ?: sak.vedtak
        )


        return sakRepository.save(oppdatertSak)
    }



}