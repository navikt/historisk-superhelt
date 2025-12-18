package no.nav.historisk.superhelt.utbetaling

import no.nav.common.types.Belop

data class UtbetalingUpdateDto(
    val belop: Belop? = null,
    val utbetalingsType: UtbetalingsType
)
