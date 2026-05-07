package no.nav.saf.graphql

import no.nav.common.consts.FellesKodeverkTema

data class DokumentoversiktFagsakVariables(
    val fagsakId: String,
    val fagsaksystem: String,
    val tema: List<FellesKodeverkTema>,
    val foerste: Int = 10
)

data class DokumentoversiktBrukerVariables(
    val fnr: String,
    val tema: List<FellesKodeverkTema>,
    val foerste: Int = 50
)

data class DokumentoversiktFagsakGraphqlResponse(
    val data: DokumentoversiktFagsakData?,
    val errors: List<GraphqlError>? = null,
)

data class DokumentoversiktFagsakData(
    val dokumentoversiktFagsak: DokumentoversiktResult
)

data class DokumentoversiktBrukerGraphqlResponse(
    val data: DokumentoversiktBrukerData?,
    val errors: List<GraphqlError>? = null,
)

data class DokumentoversiktBrukerData(
    val dokumentoversiktBruker: DokumentoversiktResult
)

data class DokumentoversiktResult(
    val journalposter: List<Journalpost>
)

