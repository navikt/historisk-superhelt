package no.nav.dokdist

import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient

class DokdistClient(
    private val restClient: RestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private class ManglerAdresseException : RuntimeException()

    fun distribuerJournalpost(request: DistribuerJournalpostRequest): DokdistRespons {
        try {
            val respons = restClient.post()
                .uri("/rest/v1/distribuerjournalpost")
                .body(request)
                .retrieve()
                .onStatus({ it.value() == 409 }) { _, _ ->
                    logger.info("Journalpost med id {} er allerede distribuert. Ignorerer", request.journalpostId)
                }
                .onStatus({ it.value() == 410 }) { _, _ ->
                    throw ManglerAdresseException()
                }
                .body(DistribuerJournalpostResponse::class.java)
            return DokdistRespons(bestillingsId = respons?.bestillingsId, sendtOk = true)
        } catch (e: ManglerAdresseException) {
            logger.warn(
                "Journalpost med id {} kan ikke distribueres: mangler adresse (410 Gone)",
                request.journalpostId,
            )
            return DokdistRespons(
                sendtOk = false,
                feilbegrunnelse = "Journalpost kan ikke distribueres: mangler adresse",
            )
        }
    }
}

