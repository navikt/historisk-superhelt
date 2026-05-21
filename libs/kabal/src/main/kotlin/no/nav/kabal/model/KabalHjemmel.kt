package no.nav.kabal.model

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Hjemler støttet av Kabal for klage/anke-oversendelse.
 * ID-er, lovKilde og spesifikasjon er hentet fra:
 * https://github.com/navikt/klage-kodeverk/blob/main/src/main/kotlin/no/nav/klage/kodeverk/hjemmel/Hjemmel.kt
 */
enum class KabalHjemmel(
    @JsonValue val id: String,
    val lovKilde: KabalLovKilde,
    val spesifikasjon: String,
) {
    // Folketrygdloven – kapittel 10
    FTRL_10_3("FTRL_10_3", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-3"),
    FTRL_10_4("FTRL_10_4", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-4"),
    FTRL_10_5("FTRL_10_5", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-5"),
    FTRL_10_6("FTRL_10_6", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-6"),
    FTRL_10_7I("FTRL_10_7I", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7i Ortopediske hjelpemidler"),
    FTRL_10_7_HJELPEMIDLER("FTRL_10_7_HJELPEMIDLER", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7 Hjelpemidler"),
    FTRL_10_7A("FTRL_10_7A", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7a"),
    FTRL_10_7A_BRILLER_TIL_BARN("FTRL_10_7A_BRILLER_TIL_BARN", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7a Briller til barn"),
    FTRL_10_7B("FTRL_10_7B", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7b Høreapparat"),
    FTRL_10_7C("FTRL_10_7C", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7c Grunnmønster"),
    FTRL_10_7D("FTRL_10_7D", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7d Førerhund"),
    FTRL_10_7E("FTRL_10_7E", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7e Lese- og sekretærhjelp"),
    FTRL_10_7F("FTRL_10_7F", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7f Tolkehjelp"),
    FTRL_10_7G("FTRL_10_7G", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7g Tolk- og ledsagerhjelp"),
    FTRL_10_7I_B("FTRL_10_7I_B", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7i HMS"),
    FTRL_10_7XA("FTRL_10_7XA", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7 Ombygging av maskiner"),
    FTRL_10_7XB("FTRL_10_7XB", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-7 Opplæringstiltak"),
    FTRL_10_8("FTRL_10_8", KabalLovKilde.FOLKETRYGDLOVEN, "§ 10-8 Bortfall av rettigheter"),

    // Folketrygdloven – kapittel 21
    FTRL_21_3("FTRL_21_3", KabalLovKilde.FOLKETRYGDLOVEN, "§ 21-3"),
    FTRL_21_7("FTRL_21_7", KabalLovKilde.FOLKETRYGDLOVEN, "§ 21-7"),
    FTRL_21_8("FTRL_21_8", KabalLovKilde.FOLKETRYGDLOVEN, "§ 21-8"),
    FTRL_21_10("FTRL_21_10", KabalLovKilde.FOLKETRYGDLOVEN, "§ 21-10"),
    FTRL_21_12("FTRL_21_12", KabalLovKilde.FOLKETRYGDLOVEN, "§ 21-12"),

    // Folketrygdloven – kapittel 22
    FTRL_22_12("FTRL_22_12", KabalLovKilde.FOLKETRYGDLOVEN, "§ 22-12"),
    FTRL_22_13("FTRL_22_13", KabalLovKilde.FOLKETRYGDLOVEN, "§ 22-13"),
    FTRL_22_14("FTRL_22_14", KabalLovKilde.FOLKETRYGDLOVEN, "§ 22-14"),
    FTRL_22_17("FTRL_22_17", KabalLovKilde.FOLKETRYGDLOVEN, "§ 22-17"),

    // Forskrift om ortopediske hjelpemidler mm.
    FS_ORT_HJE_MM_1A("FS_ORT_HJE_MM_1A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 ortopediske proteser"),
    FS_ORT_HJE_MM_1B("FS_ORT_HJE_MM_1B", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 ortoser"),
    FS_ORT_HJE_MM_1C("FS_ORT_HJE_MM_1C", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 1 fotsenger"),
    FS_ORT_HJE_MM_2F("FS_ORT_HJE_MM_2F", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 2 ortopediske sko"),
    FS_ORT_HJE_MM_3A("FS_ORT_HJE_MM_3A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 3 søknad og gyldighetstid"),
    FS_ORT_HJE_MM_4A("FS_ORT_HJE_MM_4A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 4 Pris- og leveringsavtaler"),
    FS_ORT_HJE_MM_5A("FS_ORT_HJE_MM_5A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 5 brystproteser"),
    FS_ORT_HJE_MM_6A("FS_ORT_HJE_MM_6A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 6 ansiktsdefektprotese"),
    FS_ORT_HJE_MM_7A("FS_ORT_HJE_MM_7A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 7 øyeprotese"),
    FS_ORT_HJE_MM_8G("FS_ORT_HJE_MM_8G", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 8 parykk"),
    FS_ORT_HJE_MM_9A("FS_ORT_HJE_MM_9A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 9 alminnelig fottøy"),
    FS_ORT_HJE_MM_9AA("FS_ORT_HJE_MM_9AA", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 9a overekstremitetsortoser ved revmatisme"),
    FS_ORT_HJE_MM_10A("FS_ORT_HJE_MM_10A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 10 Forhåndstilsagn"),
    FS_ORT_HJE_MM_12A("FS_ORT_HJE_MM_12A", KabalLovKilde.FORSKRIFT_OM_ORTOPEDISKE_HJELPEMIDLER_MM, "§ 12 reise"),

    // Forskrift om hjelpemidler mm.
    FS_HJE_MM_2A("FS_HJE_MM_2A", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 trening, aktivisering, stimulering og lek"),
    FS_HJE_MM_2B("FS_HJE_MM_2B", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 datautstyr"),
    FS_HJE_MM_2C("FS_HJE_MM_2C", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 synshjelpemidler"),
    FS_HJE_MM_2D("FS_HJE_MM_2D", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 briller og linser"),
    FS_HJE_MM_2E("FS_HJE_MM_2E", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 behandlingsbriller til barn"),
    FS_HJE_MM_2F("FS_HJE_MM_2F", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 hørselshjelpmidler"),
    FS_HJE_MM_2G("FS_HJE_MM_2G", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 kommunikasjon (ASK)"),
    FS_HJE_MM_2H("FS_HJE_MM_2H", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 boligtilskudd"),
    FS_HJE_MM_2I("FS_HJE_MM_2I", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 heisløsninger"),
    FS_HJE_MM_2J("FS_HJE_MM_2J", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 gå- og forflytningshjelpemidler"),
    FS_HJE_MM_2K("FS_HJE_MM_2K", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 småhjelpemidler"),
    FS_HJE_MM_2L("FS_HJE_MM_2L", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2"),
    FS_HJE_MM_2M("FS_HJE_MM_2M", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 gjeldsoppgjør"),
    FS_HJE_MM_2N("FS_HJE_MM_2N", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 alarm og varsling"),
    FS_HJE_MM_2O("FS_HJE_MM_2O", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 madrasser og puter"),
    FS_HJE_MM_2P("FS_HJE_MM_2P", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 utstyr på arbeidsplass"),
    FS_HJE_MM_2Q("FS_HJE_MM_2Q", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 varmehjelpemidler"),
    FS_HJE_MM_2R("FS_HJE_MM_2R", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 2 refusjon"),
    FS_HJE_MM_4("FS_HJE_MM_4", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 4 førerhund"),
    FS_HJE_MM_6A("FS_HJE_MM_6A", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 6 tilpasningskurs"),
    FS_HJE_MM_6D("FS_HJE_MM_6D", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 6 folkehøgskole"),
    FS_HJE_MM_7("FS_HJE_MM_7", KabalLovKilde.FORSKRIFT_OM_HJELPEMIDLER_MM, "§ 7 reise"),

    // Forskrift om høreapparater mm.
    FS_HA_MM_2A("FS_HA_MM_2A", KabalLovKilde.FORSKRIFT_OM_HØREAPPARATER_MM, "§ 2 høreapparat"),
    FS_HA_MM_3A("FS_HA_MM_3A", KabalLovKilde.FORSKRIFT_OM_HØREAPPARATER_MM, "§ 3 gjenanskaffelse"),
    FS_HA_MM_4A("FS_HA_MM_4A", KabalLovKilde.FORSKRIFT_OM_HØREAPPARATER_MM, "§ 4 krav til søknad og produkt"),
    FS_HA_MM_6A("FS_HA_MM_6A", KabalLovKilde.FORSKRIFT_OM_HØREAPPARATER_MM, "§ 6 Pris og leveringsavtaler"),
    FS_HA_MM_8A("FS_HA_MM_8A", KabalLovKilde.FORSKRIFT_OM_HØREAPPARATER_MM, "§ 8 stønad og satser"),

    // Forskrift om servicehund
    FS_SH_2("FS_SH_2", KabalLovKilde.FORSKRIFT_OM_SERVICEHUND, "§ 2 servicehund"),

    // EØS-forordning 883/2004
    EOES_883_2004_11("EOES_883_2004_11", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 11"),
    EOES_883_2004_12("EOES_883_2004_12", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 12"),
    EOES_883_2004_13("EOES_883_2004_13", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 13"),
    EOES_883_2004_23("EOES_883_2004_23", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 23"),
    EOES_883_2004_24("EOES_883_2004_24", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 24"),
    EOES_883_2004_25("EOES_883_2004_25", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 25"),
    EOES_883_2004_33("EOES_883_2004_33", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 33"),
    EOES_883_2004_81("EOES_883_2004_81", KabalLovKilde.EØS_FORORDNING_883_2004, "art. 81"),

    // Gjennomføringsforordning 987/2009
    GJ_F_FORD_987_2009_11("GJ_F_FORD_987_2009_11", KabalLovKilde.GJENNOMFØRINGSFORORDNING_987_2009, "art. 11"),

    // Nordisk konvensjon
    NORDISK_KONVENSJON("NORDISK_KONVENSJON", KabalLovKilde.NORDISK_KONVENSJON, "Nordisk konvensjon"),

    // Forvaltningsloven
    FVL_11("FVL_11", KabalLovKilde.FORVALTNINGSLOVEN, "§ 11"),
    FVL_12("FVL_12", KabalLovKilde.FORVALTNINGSLOVEN, "§ 12"),
    FVL_14("FVL_14", KabalLovKilde.FORVALTNINGSLOVEN, "§ 14"),
    FVL_16("FVL_16", KabalLovKilde.FORVALTNINGSLOVEN, "§ 16"),
    FVL_17("FVL_17", KabalLovKilde.FORVALTNINGSLOVEN, "§ 17"),
    FVL_18_19("FVL_18_19", KabalLovKilde.FORVALTNINGSLOVEN, "§ 18 og 19"),
    FVL_21("FVL_21", KabalLovKilde.FORVALTNINGSLOVEN, "§ 21"),
    FVL_24("FVL_24", KabalLovKilde.FORVALTNINGSLOVEN, "§ 24"),
    FVL_25("FVL_25", KabalLovKilde.FORVALTNINGSLOVEN, "§ 25"),
    FVL_28("FVL_28", KabalLovKilde.FORVALTNINGSLOVEN, "§ 28"),
    FVL_29("FVL_29", KabalLovKilde.FORVALTNINGSLOVEN, "§ 29"),
    FVL_30("FVL_30", KabalLovKilde.FORVALTNINGSLOVEN, "§ 30"),
    FVL_31("FVL_31", KabalLovKilde.FORVALTNINGSLOVEN, "§ 31"),
    FVL_32("FVL_32", KabalLovKilde.FORVALTNINGSLOVEN, "§ 32"),
    FVL_33("FVL_33", KabalLovKilde.FORVALTNINGSLOVEN, "§ 33"),
    FVL_35("FVL_35", KabalLovKilde.FORVALTNINGSLOVEN, "§ 35"),
    FVL_41("FVL_41", KabalLovKilde.FORVALTNINGSLOVEN, "§ 41"),
    FVL_42("FVL_42", KabalLovKilde.FORVALTNINGSLOVEN, "§ 42"),

    // Trygderettsloven
    TRRL_2("TRRL_2", KabalLovKilde.TRYGDERETTSLOVEN, "§ 2"),
    TRRL_9("TRRL_9", KabalLovKilde.TRYGDERETTSLOVEN, "§ 9"),
    TRRL_10("TRRL_10", KabalLovKilde.TRYGDERETTSLOVEN, "§ 10"),
    TRRL_11("TRRL_11", KabalLovKilde.TRYGDERETTSLOVEN, "§ 11"),
    TRRL_12("TRRL_12", KabalLovKilde.TRYGDERETTSLOVEN, "§ 12"),
    TRRL_14("TRRL_14", KabalLovKilde.TRYGDERETTSLOVEN, "§ 14"),

    // Forskrift om aktivitetshjelpemidler til de over 26 år
    FS_AKT_26_2A("FS_AKT_26_2A", KabalLovKilde.FORSKRIFT_OM_AKTIVITETSHJELPEMIDLER_TIL_DE_OVER_26_AR, "§ 2 aktivitetshjelpemidler"),
    FS_AKT_26_2B("FS_AKT_26_2B", KabalLovKilde.FORSKRIFT_OM_AKTIVITETSHJELPEMIDLER_TIL_DE_OVER_26_AR, "§ 2 utlån"),
    FS_AKT_26_4("FS_AKT_26_4", KabalLovKilde.FORSKRIFT_OM_AKTIVITETSHJELPEMIDLER_TIL_DE_OVER_26_AR, "§ 4 egenandel"),
    FS_AKT_26_5("FS_AKT_26_5", KabalLovKilde.FORSKRIFT_OM_AKTIVITETSHJELPEMIDLER_TIL_DE_OVER_26_AR, "§ 5 spesialtilpasning av ordinært utstyr"),

    // Forskrift om arbeids- og utdanningsreiser
    FS_ARB_UTD_R_2("FS_ARB_UTD_R_2", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 2"),
    FS_ARB_UTD_R_3("FS_ARB_UTD_R_3", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 3"),
    FS_ARB_UTD_R_4("FS_ARB_UTD_R_4", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 4"),
    FS_ARB_UTD_R_5("FS_ARB_UTD_R_5", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 5"),
    FS_ARB_UTD_R_6("FS_ARB_UTD_R_6", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 6"),
    FS_ARB_UTD_R_8("FS_ARB_UTD_R_8", KabalLovKilde.FORSKRIFT_OM_ARBEIDS_OG_UTDANNINGSREISER, "§ 8"),
    ;

    companion object {
        /** Hjemler per Kabal-ytelse-ID (kilde: klage-kodeverk YtelseToHjemler). */
        private val PER_YTELSE: Map<KabalYtelse, List<KabalHjemmel>> = mapOf(
            KabalYtelse.HEL_HEL to listOf(
                FTRL_10_3, FTRL_10_4, FTRL_10_5, FTRL_10_6, FTRL_10_7I, FTRL_10_8,
                FTRL_21_3, FTRL_21_7, FTRL_21_8, FTRL_21_10, FTRL_21_12,
                FTRL_22_12, FTRL_22_13, FTRL_22_14, FTRL_22_17,
                FS_ORT_HJE_MM_1A, FS_ORT_HJE_MM_1B, FS_ORT_HJE_MM_1C, FS_ORT_HJE_MM_2F,
                FS_ORT_HJE_MM_3A, FS_ORT_HJE_MM_4A, FS_ORT_HJE_MM_5A, FS_ORT_HJE_MM_6A,
                FS_ORT_HJE_MM_7A, FS_ORT_HJE_MM_8G, FS_ORT_HJE_MM_9A, FS_ORT_HJE_MM_9AA,
                FS_ORT_HJE_MM_10A, FS_ORT_HJE_MM_12A,
                EOES_883_2004_11, EOES_883_2004_12, EOES_883_2004_13, EOES_883_2004_23,
                EOES_883_2004_24, EOES_883_2004_25, EOES_883_2004_33, EOES_883_2004_81,
                GJ_F_FORD_987_2009_11, NORDISK_KONVENSJON,
                FVL_11, FVL_12, FVL_14, FVL_16, FVL_17, FVL_18_19, FVL_21, FVL_24,
                FVL_25, FVL_28, FVL_29, FVL_30, FVL_31, FVL_32, FVL_33, FVL_35, FVL_41, FVL_42,
                TRRL_2, TRRL_9, TRRL_10, TRRL_11, TRRL_12, TRRL_14,
            ),
            KabalYtelse.HJE_HJE to listOf(
                FTRL_10_3, FTRL_10_4, FTRL_10_5, FTRL_10_6,
                FTRL_10_7_HJELPEMIDLER, FTRL_10_7A, FTRL_10_7A_BRILLER_TIL_BARN,
                FTRL_10_7B, FTRL_10_7C, FTRL_10_7D,
                FTRL_10_7E, FTRL_10_7F, FTRL_10_7G, FTRL_10_7I_B, FTRL_10_7XA, FTRL_10_7XB,
                FTRL_10_8,
                FTRL_21_3, FTRL_21_7, FTRL_21_8, FTRL_21_10, FTRL_21_12,
                FTRL_22_12, FTRL_22_13, FTRL_22_14, FTRL_22_17,
                FS_AKT_26_2A, FS_AKT_26_2B, FS_AKT_26_4, FS_AKT_26_5,
                FS_HJE_MM_2A, FS_HJE_MM_2B, FS_HJE_MM_2C, FS_HJE_MM_2D, FS_HJE_MM_2E,
                FS_HJE_MM_2F, FS_HJE_MM_2G, FS_HJE_MM_2H, FS_HJE_MM_2I, FS_HJE_MM_2J,
                FS_HJE_MM_2K, FS_HJE_MM_2L, FS_HJE_MM_2M, FS_HJE_MM_2N, FS_HJE_MM_2O,
                FS_HJE_MM_2P, FS_HJE_MM_2Q, FS_HJE_MM_2R,
                FS_HJE_MM_4, FS_HJE_MM_6A, FS_HJE_MM_6D, FS_HJE_MM_7,
                FS_HA_MM_2A, FS_HA_MM_3A, FS_HA_MM_4A, FS_HA_MM_6A, FS_HA_MM_8A,
                FS_SH_2,
                EOES_883_2004_11, EOES_883_2004_12, EOES_883_2004_13, EOES_883_2004_23,
                EOES_883_2004_24, EOES_883_2004_25, EOES_883_2004_33, EOES_883_2004_81,
                GJ_F_FORD_987_2009_11, NORDISK_KONVENSJON,
                FVL_11, FVL_12, FVL_14, FVL_16, FVL_17, FVL_18_19, FVL_21, FVL_24,
                FVL_25, FVL_28, FVL_29, FVL_30, FVL_31, FVL_32, FVL_33, FVL_35, FVL_41, FVL_42,
                TRRL_2, TRRL_9, TRRL_10, TRRL_11, TRRL_12, TRRL_14,
            ),
            KabalYtelse.HJE_AUR to listOf(
                FS_ARB_UTD_R_2, FS_ARB_UTD_R_3, FS_ARB_UTD_R_4,
                FS_ARB_UTD_R_5, FS_ARB_UTD_R_6, FS_ARB_UTD_R_8,
                FVL_11, FVL_12, FVL_14, FVL_16, FVL_17, FVL_18_19, FVL_21, FVL_24,
                FVL_25, FVL_28, FVL_29, FVL_30, FVL_31, FVL_32, FVL_33, FVL_35, FVL_41, FVL_42,
                TRRL_9, TRRL_10,
            ),
        )

        fun forYtelse(kabalYtelse: KabalYtelse): List<KabalHjemmel> = PER_YTELSE[kabalYtelse] ?: emptyList()

        fun fromId(id: String): KabalHjemmel =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Hjemmel with id '$id'")
    }
}
