package no.nav.historisk.superhelt.sak.rest

import no.nav.historisk.superhelt.utbetaling.Utbetaling

data class UtbetalingDto(val belop: Double) {

}
internal fun Utbetaling?.toResponseDto(): UtbetalingDto? {
    return this?.let {
        UtbetalingDto(
            belop = this.belop,
        )
    }
}
