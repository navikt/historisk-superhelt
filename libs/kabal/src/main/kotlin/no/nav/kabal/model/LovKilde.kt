package no.nav.kabal.model

/**
 * Lovkilder brukt i Hjemmel-enum.
 * Hentet fra:
 * https://github.com/navikt/klage-kodeverk/blob/main/src/main/kotlin/no/nav/klage/kodeverk/hjemmel/LovKilde.kt
 */
enum class LovKilde(val id: String, val navn: String, val beskrivelse: String) {
    FOLKETRYGDLOVEN("1", "Folketrygdloven", "Ftrl"),
    FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM("4", "Forskrift om ortopediske hjelpemidler mm.", "Forskrift om dekning av utgifter til proteser mv."),
    EØS_FORORDNING_883_2004("11", "EØS forordning 883/2004", "EØS forordning 883/2004"),
    NORDISK_KONVENSJON("15", "Nordisk konvensjon", "Nordisk konvensjon"),
    GJENNOMFØRINGSFORORDNING_987_2009("16", "Gjennomføringsforordning 987/2009", "Gjennomføringsforordning 987/2009"),
    FORVALTNINGSLOVEN("8", "Forvaltningsloven", "Fvl"),
    TRYGDERETTSLOVEN("9", "Trygderettsloven", "Trrl"),
}
