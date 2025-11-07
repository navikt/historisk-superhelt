package no.nav.historisk.superhelt.utbetaling

enum class UtbetalingStatus {
    /** Initial status når utbetaling opprettes */
    UTKAST,

    /** Klar til å sendes, men enda ikke sendt */
    KLAR_TIL_UTBETALING,

    /** Sendt til utbetaling hos helved */
    SENDT_TIL_UTBETALING,

    /** Ferdig utbetalt og kvittert */
    UTBETALT,
//    FEILET
}
