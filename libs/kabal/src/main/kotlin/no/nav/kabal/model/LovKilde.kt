package no.nav.kabal.model

/**
 * Lovkilder brukt i Hjemmel-enum.
 * Hentet fra:
 * https://github.com/navikt/klage-kodeverk/blob/main/src/main/kotlin/no/nav/klage/kodeverk/hjemmel/LovKilde.kt
 */
enum class LovKilde(val id: String, val navn: String, val beskrivelse: String) {
    FOLKETRYGDLOVEN("1", "Folketrygdloven", "Ftrl"),
    FORSKRIFT_OM_HJELPEMIDLER_MM("3", "Forskrift om hjelpemidler mm.", "Forskrift om stønad til hjelpemidler mv."),
    FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM("4", "Forskrift om ortopediske hjelpemidler mm.", "Forskrift om dekning av utgifter til proteser mv."),
    FORSKRIFT_OM_HØREAPPARATER_MM("5", "Forskrift om høreapparater mm.", "Forskrift om stønad til høreapparat og tinnitusmaskerer"),
    FORSKRIFT_OM_SERVICEHUND("6", "Forskrift om servicehund", "Forskrift om stønad til servicehund"),
    FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER("45", "Forskrift om arbeids- og utdanningsreiser", "Forskrift om stønad til arbeids- og utdanningsreiser"),
    FORSKRIFT_OM_AKTIVITETSHJELPEMIDLER_TIL_DE_OVER_26_AR("46", "Forskrift om aktivitetshjelpemidler til de over 26 år", "Forskrift om aktivitetshjelpemidler til de over 26 år"),
    EØS_FORORDNING_883_2004("11", "EØS forordning 883/2004", "EØS forordning 883/2004"),
    NORDISK_KONVENSJON("15", "Nordisk konvensjon", "Nordisk konvensjon"),
    GJENNOMFØRINGSFORORDNING_987_2009("16", "Gjennomføringsforordning 987/2009", "Gjennomføringsforordning 987/2009"),
    FORVALTNINGSLOVEN("8", "Forvaltningsloven", "Fvl"),
    TRYGDERETTSLOVEN("9", "Trygderettsloven", "Trrl"),
}
