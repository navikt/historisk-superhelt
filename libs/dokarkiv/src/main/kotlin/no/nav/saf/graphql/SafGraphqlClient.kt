package no.nav.saf.graphql

import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Saksnummer
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

    fun dokumentoversiktFagsak(
        saksnummer: Saksnummer,
        tema: List<DokarkivTema> = listOf(DokarkivTema.HEL),
        fagsakSystem: String = "HELT"
    ): DokumentoversiktGraphqlResponse {
        val req =
            createGraphqlQuery(
                gqlFile = "/saf/dokumentoversiktFagsak.graphql",
                variables = DokumentoversiktFagsakVariables(
                    fagsakId = saksnummer.value,
                    fagsaksystem = fagsakSystem,
                    tema = tema,
                    foerste = 50,
                ),
            )
        return restClient
            .post()
            .uri("/graphql")
            .body(req)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(DokumentoversiktGraphqlResponse::class.java)!!
    }


}
