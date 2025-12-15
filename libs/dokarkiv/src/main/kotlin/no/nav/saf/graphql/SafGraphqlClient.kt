package no.nav.pdl

import no.nav.dokarkiv.EksternJournalpostId
import no.nav.saf.graphql.HentJournalpostGraphqlResponse
import no.nav.saf.graphql.JournalPostVariables
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class SafGraphqlClient(
    private val restClient: RestClient,
) {

    fun hentJournalpost(journalpostId: EksternJournalpostId): HentJournalpostGraphqlResponse {
        val req =
            createGraphqlQuery(
                gqlFile = "/saf/hentJournalpost.graphql",
                variables = JournalPostVariables(journalpostId),
            )
        return restClient
            .post()
            .uri("/graphql")
            .body(req)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(HentJournalpostGraphqlResponse::class.java)!!
    }


}
