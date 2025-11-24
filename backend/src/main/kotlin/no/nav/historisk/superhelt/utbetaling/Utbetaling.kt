package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop
import java.time.Instant
import java.util.*

data class Utbetaling(
    val belop: Belop,
    val uuid: UUID,
    val utbetalingStatus: UtbetalingStatus,
    val utbetalingTidspunkt: Instant?) {}



