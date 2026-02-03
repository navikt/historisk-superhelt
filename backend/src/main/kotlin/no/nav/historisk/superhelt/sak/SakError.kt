package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus

class SakError(sak: Sak) {
    val utbetalingError: Boolean = sak.utbetaling?.utbetalingStatus == UtbetalingStatus.FEILET

    // TODO kanskje flytte inn valideringsfeil også


}

