package no.nav.saf.graphql

import no.nav.common.consts.APP_NAVN
import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.FolkeregisterIdent
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
        tema: List<FellesKodeverkTema>,
        fagsakSystem: String = APP_NAVN
    ): DokumentoversiktFagsakGraphqlResponse {

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
            .body(DokumentoversiktFagsakGraphqlResponse::class.java)!!
    }

    fun dokumentoversiktBruker(
        fnr: FolkeregisterIdent,
        tema: List<FellesKodeverkTema>,
    ): DokumentoversiktBrukerGraphqlResponse {

        val req =
            createGraphqlQuery(
                gqlFile = "/saf/dokumentoversiktBruker.graphql",
                variables = DokumentoversiktBrukerVariables(
                    fnr = fnr.value,
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
            .body(DokumentoversiktBrukerGraphqlResponse::class.java)!!
    }


}
