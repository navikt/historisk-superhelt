package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {

    @Transactional(readOnly = true)
    internal fun finnBrev(sak: Sak, type: BrevType, mottaker: BrevMottaker): Brev? {
        val brevForSak = brevRepository.findBySak(sak.saksnummer)

        return brevForSak.findBrev(type = type, mottaker = mottaker)
    }

    @Transactional
    internal fun genererNyttBrev(
        sak: Sak,
        type: BrevType,
        mottaker: BrevMottaker): Brev {
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