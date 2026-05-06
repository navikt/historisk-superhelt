package no.nav.helved

/**
 * Klassekoder for helved utbetalinger.
 *
 * https://github.com/navikt/helved-utbetaling/blob/main/models/main/models/Utbetalinger.kt#L425
 */

enum class KlasseKode(internal val klassekode: String, val navn: String) {
    @Deprecated("Brukes ikke lenger i helved")
    TILSKUDD_SMÅHJELPEMIDLER("HJRIM", "Tilskudd småhjelpemidler"),

    /* HelseTjensester */
    REISEUTGIFTER("HTRUTR", "Reiseutgifter"),
    ORTOPEDISK_PROTESE("HTOHPR", "Ortopedisk protese"),
    ORTOSE("HTOHHÅ", "Ortose"),
    SPESIALSKO("HTOHSKBA", "Spesialsko"),
    PARYKK("HTOHPAAV", "Parykk"),
    ANSIKTSDEFEKTPROTESE("HTOHAD", "Ansiktsdefektprotese"),
    BRYSTPROTESE("HTOHBP", "Brystprotese"),
    ØYEPROTESE("HTOHØP", "Øyeprotese"),
    VANLIGE_SKO("HTOHAS", "Vanlige sko"),
    FOTSENG("HTOHFTEN", "Fotseng"),

    /* Hjelpemidler*/
    HØREAPPARAT_ANSKAFFELSE("H-APP", "Høreapparat anskaffelse"),
    HØREAPPARAT_REPARASJON("H-APPREP", "Høreapparat reparasjon"),
    HØREAPPARAT_EGENBETALING("H-APPEGENBET", "Høreapparat egenbetaling"),
    LEGEERKLÆRING_SPESIALIST("LEGEERKLSPES", "Legeerklæring spesialist"),
    LEGEERKLÆRING_ALLMENN("LEGEERKLALM", "Legeerklæring allmenn"),
    ARBEIDSPLASSVURDERING_FYSIOTERAPEUT("ARBPLVURD", "Arbeidsplassvurdering fysioterapeut"),
    OPPLÆRING_TILPASNING_KURS_SYN("OPPLTILP-SYN", "Opplæring og tilpasning kurs syn"),
    OPPLÆRING_TILPASNING_KURS_HØRSEL("OPPLTILP-HORSEL", "Opplæring og tilpasning kurs hørsel"),
    OPPLÆRING_TILPASNING_KURS_DØVBLIND("OPPLTILP-DOVBLIND", "Opplæring og tilpasning kurs døvblind"),
    OPPLÆRING_TILPASNING_FOLKEHØGSKOLE("OPPLTILP-FHSKOLE", "Opplæring og tilpasning folkehøgskole"),
    OPPLÆRING_TILPASNING_BRISKEBY("OPPLTILP-BRISKEBY", "Opplæring og tilpasning Briskeby"),
    TINNITUSMASKERER("HJTINNITUS", "Tinnitusmaskerer"),
    HJELPEMIDLER_SELVSTENDIG_NÆRINGSDRIVENDE("HJARBLIVSELVST", "Hjelpemidler selvstendig næringsdrivende"),
    HJELPEMIDLER_ARBEID_UTDANNING("HJARBUTD", "Hjelpemidler arbeid og utdanning"),
    HJELPEMIDLER_ATTFØRING("HJATTF", "Hjelpemidler attføring"),
    HJELPEMIDLER_GRUNNMØNSTER("HJGRUNNM", "Hjelpemidler grunnmønster"),
    HJELPEMIDLER_ANNET("HJANNET", "Hjelpemidler annet"),
    SEKSUALTEKNISKE_HJELPEMIDLER("HJSEXTEKN", "Seksualtekniske hjelpemidler"),
    REISE_OPPHOLD("REOPPH", "Reise og opphold"),
    REISE_OPPHOLD_BIL("REOPPH-BIL", "Reise og opphold bil"),
    REISE_OPPHOLD_HJELPEMIDLER("REOPPH-HJ", "Reise og opphold hjelpemidler"),
    REISE_OPPHOLD_ORTOPEDISKE_HJELPEMIDLER("REOPPH-ORT", "Reise og opphold ortopediske hjelpemidler"),
    TAPT_ARBEIDSFORTJENESTE_LEDSAGER("TAPTARBLEDS", "Tapt arbeidsfortjeneste ledsager"),
    TAPT_ARBEIDSFORTJENESTE_LEDSAGER_IOP("TAPTARBLEDS-IOP", "Tapt arbeidsfortjeneste ledsager IOP"),
    REPARASJON_HJELPEMIDLER_UTLAND("HJREP", "Reparasjon hjelpemidler utland"),
    SYNSHJELPEMIDLER("HJSYN", "Synshjelpemidler"),
    BOLIGTILSKUDD("HJBOL", "Boligtilskudd"),
    FØRERHUND_VETERINÆR("HJHUND", "Førerhund veterinær"),
    ØREPROPPER("HJPROPP", "Ørepropper"),
    BILTILSKUDD_GRUPPE_1("HJBILGR1", "Biltilskudd gruppe 1"),
    KJØREOPPLÆRING_GRUPPE_1("HJBILOPPLGR1", "Kjøreopplæring gruppe 1"),
    KJØREOPPLÆRING_GRUPPE_2("HJBILOPPLGR2", "Kjøreopplæring gruppe 2"),
    BILTILSKUDD_GRUPPE_2("HJBILGR2", "Biltilskudd gruppe 2"),
    DATAUTSTYR("HJDATA", "Datautstyr"),
    SERVICEHUND_REISEUTGIFTER("SEHUNDREIS", "Servicehund reiseutgifter"),
    SERVICEHUND_VETERINÆR("SEHUNDVET", "Servicehund veterinær"),
    APP_KOGNISJON("HJAPPKOG", "App kognisjon"),
    APP_KOMMUNIKASJON("HJAPPKOM", "App kommunikasjon"),
    APP_LESE_OG_SKRIVESTØTTE("HJAPPLES", "App lese og skrivestøtte"),
    APP_SYN("HJAPPSYN", "App syn"),
    BEHANDLINGSBRILLE_SATS_1("HTBDBR", "Behandlingsbrille sats 1"),
    BEHANDLINGSBRILLE_SATS_2("HTBDBI", "Behandlingsbrille sats 2"),
    BEHANDLINGSBRILLE_INDIVIDUELL("HTBDIB", "Behandlingsbrille individuell"),
    KONTAKTLINSER_BEHANDLING("HTBDKB", "Kontaktlinser behandling"),
    REPARASJON_BEHANDLINGSBRILLE("HTBDRB", "Reparasjon behandlingsbrille"),
}
