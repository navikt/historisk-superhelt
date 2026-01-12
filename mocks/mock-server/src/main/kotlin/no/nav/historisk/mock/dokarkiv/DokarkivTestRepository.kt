package no.nav.historisk.mock.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.saf.graphql.Journalpost
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DokarkivTestRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = mutableMapOf<EksternJournalpostId, Journalpost>()

    fun lagre(
        journalpostId: EksternJournalpostId,
        data: Journalpost,
    ): EksternJournalpostId {
        logger.debug("Lagrer journalpostId {} med data {}", journalpostId, data)
        repository[journalpostId] = data
        return journalpostId
    }

    fun findOrCreate(journalpostId: EksternJournalpostId): Journalpost =
        repository[journalpostId]
            ?: generateAndCacheResponse(journalpostId)

    private fun generateAndCacheResponse(journalpostId: EksternJournalpostId): Journalpost {
        val harVedlegg = faker.number().numberBetween(0, 100) > 70
        val response = generateJournalpost(journalpostId, if (harVedlegg) 2 else 0)
        lagre(journalpostId, response)
        logger.debug("Registerer ny journalpost i saf-mock: {} -> {}", journalpostId, response)
        return response
    }
}
