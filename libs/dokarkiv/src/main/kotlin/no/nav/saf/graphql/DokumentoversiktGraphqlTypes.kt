package no.nav.saf.graphql

import no.nav.common.consts.EksternFellesKodeverkTema

data class DokumentoversiktFagsakVariables(
    val fagsakId: String,
    val fagsaksystem: String,
    val tema: List<EksternFellesKodeverkTema>,
    val foerste: Int = 10
)

data class DokumentoversiktGraphqlResponse(
    val data: DokumentoversiktData?,
    val errors: List<GraphqlError>? = null,
)

data class DokumentoversiktData(
    val dokumentoversiktFagsak: DokumentoversiktFagsakResult
)
data class DokumentoversiktFagsakResult(
    val journalposter: List<Journalpost>
)
