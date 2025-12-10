package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.brev.rest.OppdaterBrevRequest
import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {

    @Transactional
    fun hentEllerOpprettBrev(sak: Sak, type: BrevType, mottaker: BrevMottaker): BrevUtkast {
        brevRepository.findBySak(sak.saksnummer).findBrev(type = type, mottaker = mottaker)?.let {
            return it
        }
        val brevTekstGenerator= BrevTekstGenerator(sak)

        val brevUtkast = BrevUtkast(
            uuid = BrevId.random(),
            tittel = brevTekstGenerator.generateTittel(type, mottaker),
            innhold = brevTekstGenerator.generateInnhold(type, mottaker),
            type = type,
            mottakerType = mottaker,
        )
        return  brevRepository.opprettBrev(sak.saksnummer, brevUtkast)
    }

    @Transactional
    fun oppdaterBrev(uuid: BrevId, request: OppdaterBrevRequest): BrevUtkast {
        val brev = brevRepository.getByUUid(uuid)
        if (brev.status == BrevStatus.SENDT) {
            throw IllegalStateException("Kan ikke oppdatere brev som er sendt")
        }
        val oppdatertBrev = brev.copy(
            tittel = request.tittel ?: brev.tittel,
            innhold = request.innhold ?: brev.innhold,
            status = BrevStatus.UNDER_ARBEID
        )
        return brevRepository.lagre(oppdatertBrev)
    }
}