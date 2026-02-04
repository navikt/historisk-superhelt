package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRettighet
import no.nav.historisk.superhelt.sak.SakValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {

    @Transactional
    fun hentEllerOpprettBrev(sak: Sak, type: BrevType, mottaker: BrevMottaker): Brev {
        val brevForSak = brevRepository.findBySak(sak.saksnummer)

        val brev = brevForSak.findBrev(type = type, mottaker = mottaker)
        if (brev != null) {
            return brev
        }

        return genererNyttBrev(sak, type, mottaker)
    }

    private fun genererNyttBrev(
        sak: Sak,
        type: BrevType,
        mottaker: BrevMottaker): Brev {
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        val brevTekstGenerator = BrevTekstGenerator(sak)

        val brev = Brev(
            uuid = BrevId.random(),
            tittel = brevTekstGenerator.generateTittel(type, mottaker),
            innhold = brevTekstGenerator.generateInnhold(type, mottaker),
            type = type,
            mottakerType = mottaker,
            saksnummer = sak.saksnummer,
        )
        return brevRepository.opprettBrev(brev)
    }

}