package no.nav.dokdist

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class DokdistClient(
    private val restClient: RestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun distribuerJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponse {
        val respons = restClient.post()
            .uri("/rest/v1/distribuerjournalpost")
            .body(request)
            .retrieve()
            .onStatus({ it.value() == 409 }) { _, _ ->
                logger.info("Journalpost med id {} er allerede distribuert. Ignorerer", request.journalpostId)
            }
            .onStatus({ it.value() == 410 }) { _, _ ->
                throw HttpClientErrorException(
                    HttpStatus.GONE,
                    "Journalpost med id ${request.journalpostId} kan ikke distribueres. Bruker er død og har ukjent postadresse."

                )
            }
            .body<DistribuerJournalpostResponse>()
        return respons!!
    }
}

