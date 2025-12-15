package no.nav.dokdist

import org.springframework.web.client.RestClient

class DokdistClient(
    private val restClient: RestClient,
) {

    fun distribuerJournalpost(request: DistribuerJournalpostRequest): DistribuerJournalpostResponse {
        return restClient.post()
            .uri("/rest/v1/distribuerjournalpost")
            .body(request)
            .retrieve()
            .body(DistribuerJournalpostResponse::class.java)!!
    }
}

