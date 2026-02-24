package no.nav.historisk.superhelt.brev

import no.nav.historisk.superhelt.sak.Sak
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class BrevService(
    private val brevRepository: BrevRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
            opprettetTidspunkt = Instant.now()
        )

        logger.info("Genererer nytt brev med id ${brev.uuid} for sak ${sak.saksnummer} av type ${brev.type} og mottaker ${brev.mottakerType}")

        return brevRepository.opprettBrev(brev)
    }

}