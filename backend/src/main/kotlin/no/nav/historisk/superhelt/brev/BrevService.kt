package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {

    @Transactional
    fun hentEllerOpprettBrev(sak: Sak, type: BrevType, mottaker: BrevMottaker): Brev {
        brevRepository.findBySak(sak.saksnummer)
            .findEditableBrev(type = type, mottaker = mottaker)?.let {
                return it
            }
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