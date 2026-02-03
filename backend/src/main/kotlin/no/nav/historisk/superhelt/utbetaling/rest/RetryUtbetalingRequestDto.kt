package no.nav.historisk.superhelt.utbetaling.rest

import java.util.*

data class RetryUtbetalingRequestDto(
    val utbetalingIds: List<UUID>
)

