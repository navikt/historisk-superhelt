package no.nav.historisk.mock.pdl

data class GraphqlQuery<T>(
    val query: String,
    val variables: T
)

