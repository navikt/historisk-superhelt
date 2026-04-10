package no.nav.infotrygd

import no.nav.common.types.FolkeregisterIdent
import java.time.LocalDate

data class InfotrygdHistorikkRequest(
    val fnr: Set<FolkeregisterIdent>,
)

data class PersonkortOversiktsdetalj(
    val dato: LocalDate?,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val tekst: String?,
    val kontonummer: String? = null,
    val bevilgetBelop: String? = null,
    val betaltBelop: String? = null,
)

data class InfotrygdHistorikkResponse(
    val personkort: List<PersonkortOversiktsdetalj>,
)
