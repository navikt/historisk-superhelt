package no.nav.dokdist

import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient

class DokdistClient(
    private val restClient: RestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun distribuerJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponse {
        return restClient.post()
            .uri("/rest/v1/distribuerjournalpost")
            .body(request)
            .retrieve()
            .onStatus({ it.value() == 409 }) { _, _ ->
                // 409 Conflict is acceptable, no action needed
                logger.info("Journalpost med id {} er allerede distribuert. Ignorer", request.journalpostId)
            }
            .body(DistribuerJournalpostResponse::class.java)!!
    }
}

