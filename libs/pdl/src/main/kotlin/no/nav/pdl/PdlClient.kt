package no.nav.pdl

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

class PdlClient(private val restClient: RestClient, private val behandlingsnummer: String) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun getPersonOgIdenter(ident: String): PdlData? {
        val req = pdlRequest(ident)
        val response: HentPdlResponse = restClient.post()
            .uri("/graphql")
            .body(req)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Behandlingsnummer", behandlingsnummer)
            .retrieve()
            .body(HentPdlResponse::class.java)!!
        handlePdlErrors(response.errors)
        return response.data
    }

    private fun pdlRequest(ident: String): GraphqlQuery {
        val query = GraphqlQuery::class.java.getResource("/pdl/hentPersonOgIdenter.graphql")!!
            .readText().replace("[\n\r]", "")
        return GraphqlQuery(query, Variables(ident))
    }

    /** Oversetter PDL-feilkoder til rest api med passende HTTP-status. */
    private fun handlePdlErrors(errors: PdlErrorList?) {
        if (errors?.isNotEmpty()==true) {

            val feilmelding = errors
                .joinToString(separator = ", ") { "${it.extensions.code} \"${it.path}\" \"${it.message}\"" }
            logger.info("Feilmeldinger fra PDL: $feilmelding")

            errors.finnFeil(PdlFeilkoder.UNAUTHORIZED)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Ikke tilgang til person i PDL: ${it.message}"
                )
            }
            errors.finnFeil(PdlFeilkoder.NOT_FOUND)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Person ikke funnet: ${it.message}"
                )
            }
            errors.finnFeil(PdlFeilkoder.SERVER_ERROR)?.let {
                throw HttpClientErrorException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Feil mot PDL: ${it.message}"
                )
            }
            require(errors.isEmpty()) { "Fikk feilmeldinger fra PDL: $feilmelding" }
        }
    }

    private fun PdlErrorList.finnFeil(pdlFeilkode: String): PdlError? {
        return this.find { it.extensions.code == pdlFeilkode }
    }
}
