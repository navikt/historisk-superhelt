package no.nav.historisk.superhelt.utbetaling

import java.time.Instant
import java.util.*

data class Utbetaling(
    val belop: Int,
    val uuid: UUID,
    val utbetalingStatus: UtbetalingStatus,
    val utbetalingTidspunkt: Instant?) {}



