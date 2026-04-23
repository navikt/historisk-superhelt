package no.nav.entraproxy

import no.nav.common.types.Enhetsnummer

data class Enhet(
    val enhetnummer: Enhetsnummer,
    val navn: String,
)
