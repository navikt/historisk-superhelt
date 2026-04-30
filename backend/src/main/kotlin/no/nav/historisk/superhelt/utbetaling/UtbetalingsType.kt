package no.nav.historisk.superhelt.utbetaling

enum class UtbetalingsType {
    /** Utbetaling direkte til bruker */
    BRUKER,
    /** Gir rettighet til å sende NAV en faktura */
    FORHANDSTILSAGN,
    /** ingen utbetaling er valgt */
    INGEN,
}
