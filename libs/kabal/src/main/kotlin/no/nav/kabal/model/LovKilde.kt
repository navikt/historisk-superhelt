package no.nav.kabal.model

/**
 * Lovkilder brukt i Hjemmel-enum.
 * Hentet fra:
 * https://github.com/navikt/klage-kodeverk/blob/main/src/main/kotlin/no/nav/klage/kodeverk/hjemmel/LovKilde.kt
 */
enum class LovKilde(val navn: String, val beskrivelse: String) {
    FOLKETRYGDLOVEN("Folketrygdloven", "Ftrl"),
    FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM(
        "Forskrift om ortopediske hjelpemidler mm.",
        "Forskrift om dekning av utgifter til proteser mv.",
    ),
    ES_FORORDNING_883_2004("EØS forordning 883/2004", "EØS forordning 883/2004"),
    GJENNOMFRINGSFORORDNING_987_2009("Gjennomføringsforordning 987/2009", "Gjennomføringsforordning 987/2009"),
    NORDISK_KONVENSJON("Nordisk konvensjon", "Nordisk konvensjon"),
    FORVALTNINGSLOVEN("Forvaltningsloven", "Fvl"),
    TRYGDERETTSLOVEN("Trygderettsloven", "Trrl"),
}
