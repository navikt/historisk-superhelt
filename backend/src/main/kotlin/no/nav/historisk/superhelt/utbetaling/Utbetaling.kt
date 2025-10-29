package no.nav.historisk.superhelt.utbetaling

import no.nav.person.Fnr

data class Utbetaling(
    val id: Long? = null,
    val bruker: Fnr,
    val belop: Double,
) {
}