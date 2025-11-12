package no.nav.historisk.superhelt.utbetaling

/** Status for utbetaling i utbetalingsprosessen
 *
 * Opptrer som en slags tilstandsmaskin der status kun kan gå fremover i prosessen.
 * */
enum class UtbetalingStatus(private val level: Int) {
    /** Initial status når utbetaling opprettes */
    UTKAST(level = 0),

    /** Klar til å sendes, men enda ikke sendt */
    KLAR_TIL_UTBETALING(level = 1),

    /** Sendt til utbetaling hos helved */
    SENDT_TIL_UTBETALING(level = 2),

    /** Mottatt av helved */
    MOTTATT_AV_UTBETALING(level = 3),

    /** Hos Oppdrag */
    BEHANDLET_AV_UTBETALING(level = 4),

    /** Ferdig håndtert og godkjent hos oppdrag Endelig status */
    UTBETALT(level = 200),

    /** Feilet under utbetaling  Endelig status*/
    FEILET(level = 400);

    fun isFinal(): Boolean {
        return this == UTBETALT || this == FEILET
    }

    fun isAfter(other: UtbetalingStatus): Boolean {
        return this.level > other.level
    }
}
