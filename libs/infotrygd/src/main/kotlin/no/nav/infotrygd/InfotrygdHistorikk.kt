package no.nav.infotrygd

import java.time.LocalDate

data class InfotrygdHistorikk(
    val dato: LocalDate?,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val tekst: String?,
    val kontonummer: String,
    val kontonavn: String,
    val belop: String? = null,
)
