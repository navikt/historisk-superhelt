package no.nav.historisk.mock.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.saf.graphql.Journalpost
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DokarkivTestRepository {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val repository = mutableMapOf<EksternJournalpostId, JournalpostMedPdf>()

    fun lagre(
        journalpostId: EksternJournalpostId,
        journalpost: Journalpost,
        pdf: ByteArray?,
    ): EksternJournalpostId {
        logger.debug("Lagrer journalpostId {} med data {}", journalpostId, journalpost)
        repository[journalpostId] = JournalpostMedPdf(journalpost, pdf)
        return journalpostId
    }

    fun oppdater(
        journalpostId: EksternJournalpostId,
        journalpost: Journalpost,
    ): EksternJournalpostId {
        logger.debug("Oppdaterer journalpostId {} med data {}", journalpostId, journalpost)
        repository[journalpostId]?.let {
            repository[journalpostId] = it.copy(journalpost = journalpost)
        }
        return journalpostId
    }

    fun getPdf(journalpostId: EksternJournalpostId): ByteArray =
        repository[journalpostId]?.pdf ?: pdfdoc

    fun findOrCreate(journalpostId: EksternJournalpostId): Journalpost =
        repository[journalpostId]?.journalpost
            ?: generateAndCacheResponse(journalpostId)

    fun finnJournalposterForSak(saksnummer: String): List<Journalpost> =
        repository.values.map { it.journalpost }.filter { it.sak?.fagsakId == saksnummer }

    private fun generateAndCacheResponse(journalpostId: EksternJournalpostId): Journalpost {
        val harVedlegg = faker.number().numberBetween(0, 100) > 70
        val response = generateJournalpost(journalpostId, if (harVedlegg) 2 else 0)
        lagre(journalpostId, response, null)
        logger.debug("Registerer ny journalpost i saf-mock: {} -> {}", journalpostId, response)
        return response
    }

    data class JournalpostMedPdf(val journalpost: Journalpost, val pdf: ByteArray? = null) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as JournalpostMedPdf

            return journalpost == other.journalpost
        }

        override fun hashCode(): Int {
            return journalpost.hashCode()
        }
    }
}
