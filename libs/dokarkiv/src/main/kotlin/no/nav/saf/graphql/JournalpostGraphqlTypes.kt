package no.nav.saf.graphql

import no.nav.dokarkiv.EksternJournalpostId

data class JournalPostVariables(
    val journalpostId: EksternJournalpostId,
)

data class HentJournalpostGraphqlResponse(
    val data: HentJournalpostData?,
    val errors: List<GraphqlError>? = null,
)

data class HentJournalpostData(
    val journalpost: Journalpost?,
)
