package no.nav.kabal.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Hjemler støttet av Kabal for klage/anke-oversendelse.
 * ID-er, lovKilde og spesifikasjon er hentet fra:
 * https://github.com/navikt/klage-kodeverk/blob/main/src/main/kotlin/no/nav/klage/kodeverk/hjemmel/Hjemmel.kt
 */
enum class Hjemmel(
    @JsonValue val id: String,
    val lovKilde: LovKilde,
    val spesifikasjon: String,
) {
    // Folketrygdloven – kapittel 10
    FTRL_10_3("FTRL_10_3", LovKilde.FOLKETRYGDLOVEN, "§ 10-3"),
    FTRL_10_4("FTRL_10_4", LovKilde.FOLKETRYGDLOVEN, "§ 10-4"),
    FTRL_10_5("FTRL_10_5", LovKilde.FOLKETRYGDLOVEN, "§ 10-5"),
    FTRL_10_6("FTRL_10_6", LovKilde.FOLKETRYGDLOVEN, "§ 10-6"),
    FTRL_10_7I("FTRL_10_7I", LovKilde.FOLKETRYGDLOVEN, "§ 10-7i Ortopediske hjelpemidler"),
    FTRL_10_8("FTRL_10_8", LovKilde.FOLKETRYGDLOVEN, "§ 10-8 Bortfall av rettigheter"),

    // Folketrygdloven – kapittel 21
    FTRL_21_3("108", LovKilde.FOLKETRYGDLOVEN, "§ 21-3"),
    FTRL_21_7("FTRL_21_7", LovKilde.FOLKETRYGDLOVEN, "§ 21-7"),
    FTRL_21_8("FTRL_21_8", LovKilde.FOLKETRYGDLOVEN, "§ 21-8"),
    FTRL_21_10("FTRL_21_10", LovKilde.FOLKETRYGDLOVEN, "§ 21-10"),
    FTRL_21_12("FTRL_21_12", LovKilde.FOLKETRYGDLOVEN, "§ 21-12"),

    // Folketrygdloven – kapittel 22
    FTRL_22_12("1000.022.012", LovKilde.FOLKETRYGDLOVEN, "§ 22-12"),
    FTRL_22_13("1000.022.013", LovKilde.FOLKETRYGDLOVEN, "§ 22-13"),
    FTRL_22_14("FTRL_22_14", LovKilde.FOLKETRYGDLOVEN, "§ 22-14"),
    FTRL_22_17("FTRL_22_17", LovKilde.FOLKETRYGDLOVEN, "§ 22-17"),

    // Forskrift om ortopediske hjelpemidler mm.
    FS_ORT_HJE_MM_1A("FS_ORT_HJE_MM_1A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 ortopediske proteser"),
    FS_ORT_HJE_MM_1B("FS_ORT_HJE_MM_1B", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 ortoser"),
    FS_ORT_HJE_MM_1C("FS_ORT_HJE_MM_1C", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 fotsenger"),
    FS_ORT_HJE_MM_2F("FS_ORT_HJE_MM_2F", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 2 ortopediske sko"),
    FS_ORT_HJE_MM_3A("FS_ORT_HJE_MM_3A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 3 søknad og gyldighetstid"),
    FS_ORT_HJE_MM_4A("FS_ORT_HJE_MM_4A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 4 Pris- og leveringsavtaler"),
    FS_ORT_HJE_MM_5A("FS_ORT_HJE_MM_5A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 5 brystproteser"),
    FS_ORT_HJE_MM_6A("FS_ORT_HJE_MM_6A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 6 ansiktsdefektprotese"),
    FS_ORT_HJE_MM_7A("FS_ORT_HJE_MM_7A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 7 øyeprotese"),
    FS_ORT_HJE_MM_8G("FS_ORT_HJE_MM_8G", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 8 parykk"),
    FS_ORT_HJE_MM_9A("FS_ORT_HJE_MM_9A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 9 alminnelig fottøy"),
    FS_ORT_HJE_MM_9AA("FS_ORT_HJE_MM_9AA", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 9a overekstremitetsortoser ved revmatisme"),
    FS_ORT_HJE_MM_10A("FS_ORT_HJE_MM_10A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 10 Forhåndstilsagn"),
    FS_ORT_HJE_MM_12A("FS_ORT_HJE_MM_12A", LovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 12 reise"),

    // EØS-forordning 883/2004
    EOES_883_2004_11("EOES_883_2004_11", LovKilde.ES_FORORDNING_883_2004, "art. 11"),
    EOES_883_2004_12("EOES_883_2004_12", LovKilde.ES_FORORDNING_883_2004, "art. 12"),
    EOES_883_2004_13("EOES_883_2004_13", LovKilde.ES_FORORDNING_883_2004, "art. 13"),
    EOES_883_2004_23("EOES_883_2004_23", LovKilde.ES_FORORDNING_883_2004, "art. 23"),
    EOES_883_2004_24("EOES_883_2004_24", LovKilde.ES_FORORDNING_883_2004, "art. 24"),
    EOES_883_2004_25("EOES_883_2004_25", LovKilde.ES_FORORDNING_883_2004, "art. 25"),
    EOES_883_2004_33("EOES_883_2004_33", LovKilde.ES_FORORDNING_883_2004, "art. 33"),
    EOES_883_2004_81("EOES_883_2004_81", LovKilde.ES_FORORDNING_883_2004, "art. 81"),

    // Gjennomfringsforordning 987/2009
    GJ_F_FORD_987_2009_11("GJ_F_FORD_987_2009_11", LovKilde.GJENNOMFRINGSFORORDNING_987_2009, "art. 11"),

    // Nordisk konvensjon
    NORDISK_KONVENSJON("NORDISK_KONVENSJON", LovKilde.NORDISK_KONVENSJON, "Nordisk konvensjon"),

    // Forvaltningsloven
    FVL_11("FVL_11", LovKilde.FORVALTNINGSLOVEN, "§ 11"),
    FVL_12("FVL_12", LovKilde.FORVALTNINGSLOVEN, "§ 12"),
    FVL_14("FVL_14", LovKilde.FORVALTNINGSLOVEN, "§ 14"),
    FVL_16("FVL_16", LovKilde.FORVALTNINGSLOVEN, "§ 16"),
    FVL_17("FVL_17", LovKilde.FORVALTNINGSLOVEN, "§ 17"),
    FVL_18_19("FVL_18_19", LovKilde.FORVALTNINGSLOVEN, "§ 18 og 19"),
    FVL_21("FVL_21", LovKilde.FORVALTNINGSLOVEN, "§ 21"),
    FVL_24("FVL_24", LovKilde.FORVALTNINGSLOVEN, "§ 24"),
    FVL_25("FVL_25", LovKilde.FORVALTNINGSLOVEN, "§ 25"),
    FVL_28("FVL_28", LovKilde.FORVALTNINGSLOVEN, "§ 28"),
    FVL_29("FVL_29", LovKilde.FORVALTNINGSLOVEN, "§ 29"),
    FVL_30("FVL_30", LovKilde.FORVALTNINGSLOVEN, "§ 30"),
    FVL_31("FVL_31", LovKilde.FORVALTNINGSLOVEN, "§ 31"),
    FVL_32("FVL_32", LovKilde.FORVALTNINGSLOVEN, "§ 32"),
    FVL_33("FVL_33", LovKilde.FORVALTNINGSLOVEN, "§ 33"),
    FVL_35("FVL_35", LovKilde.FORVALTNINGSLOVEN, "§ 35"),
    FVL_41("FVL_41", LovKilde.FORVALTNINGSLOVEN, "§ 41"),
    FVL_42("FVL_42", LovKilde.FORVALTNINGSLOVEN, "§ 42"),

    // Trygderettsloven
    TRRL_2("TRRL_2", LovKilde.TRYGDERETTSLOVEN, "§ 2"),
    TRRL_9("TRRL_9", LovKilde.TRYGDERETTSLOVEN, "§ 9"),
    TRRL_10("TRRL_10", LovKilde.TRYGDERETTSLOVEN, "§ 10"),
    TRRL_11("TRRL_11", LovKilde.TRYGDERETTSLOVEN, "§ 11"),
    TRRL_12("TRRL_12", LovKilde.TRYGDERETTSLOVEN, "§ 12"),
    TRRL_14("TRRL_14", LovKilde.TRYGDERETTSLOVEN, "§ 14"),
    ;

    companion object {
        fun fromId(id: String): Hjemmel =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Hjemmel with id '$id'")
    }
}
