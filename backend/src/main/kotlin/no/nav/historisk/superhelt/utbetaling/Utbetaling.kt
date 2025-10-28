package no.nav.historisk.superhelt.utbetaling

import no.nav.person.Fnr
import java.time.Instant

data class Utbetaling(
    val bruker: Fnr,
    val belop: Double,
){
}