package no.nav.pdl


data class GraphqlQuery(
    val query: String, val variables: Variables
)

data class Variables(
    val ident: String,
)
