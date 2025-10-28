package no.nav.historisk.superhelt.utbetaling

import java.time.Instant

data class Forhandstilsagn(
//    val maxbelop: Double
    val opprettet: Instant= Instant.now(),
)