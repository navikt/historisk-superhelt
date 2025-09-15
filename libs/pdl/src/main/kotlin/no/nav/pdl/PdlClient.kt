package no.nav.pdl

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class PdlClient(private val restClient: RestClient, private val behandlingsnummer: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getPersonOgIdenter(ident: String): HentPdlResponse {
        val req = pdlRequest(ident)
        val response: HentPdlResponse = restClient.post()
            .uri("/graphql")
            .body(req)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Behandlingsnummer", behandlingsnummer)
            .retrieve()
            .body(HentPdlResponse::class.java)!!
        return response
    }

    private fun pdlRequest(ident: String): GraphqlQuery {
        val query = GraphqlQuery::class.java.getResource("/pdl/hentPersonOgIdenter.graphql")!!
            .readText().replace("[\n\r]", "")
        return GraphqlQuery(query, Variables(ident))
    }

}
