package no.nav.pdl

typealias PdlErrorList = List<PdlError>

// https://pdl-docs.ansatt.nav.no/ekstern/index.html#_feilmeldinger_fra_pdl_api_graphql_response_errors
object PdlFeilkoder {
    val UNAUTHORIZED = "unauthorized"
    val NOT_FOUND = "not_found"
    val SERVER_ERROR = "server_error"
}