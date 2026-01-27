package no.nav.helved

/**
 * Klassekoder for helved utbetalinger.
 *
 * https://github.com/navikt/helved-utbetaling/blob/main/models/main/models/Utbetalinger.kt#L425
 */

enum class KlasseKode(private val klassekode: String) {
    @Deprecated("Brukes ikke lenger i helved") // HJRI
    TILSKUDD_SMÅHJELPEMIDLER("HJRIM"),// HJRIM

    REISEUTGIFTER("HTRUTR"),
    ORTOPEDISK_PROTESE("HTOHPR"),
    ORTOSE("HTOHHÅ"),
    SPESIALSKO("HTOHSKBA"),
    PARYKK("HTOHPAAV"),
    ANSIKTSDEFEKTPROTESE("HTOHAD"),
    BRYSTPROTESE("HTOHBP"),
    ØYEPROTESE("HTOHØP"),
    VANLIGE_SKO("HTOHAS"),
    FOTSENG("HTOHFTEN")
}