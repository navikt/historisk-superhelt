package no.nav.infotrygd

/**
 * Kategorier hentet fra InfoTrygd, med tilhørende koder.
 *
 * [kode] er den primære InfoTrygd-koden.
 * [gammelKode] er en eldre alternativ kode for samme kategori, der denne finnes.
 * [navn] er kategoriens lesbare navn.
 */
enum class InfotrygdKontonummer(val kode: String, val gammelKode: String? = null, val navn: String) {
    ORTOSE("5122000", "2510002", "Ortose"),
    PROTESE("5122100", "2510001", "Protese"),
    SPESIALSKO("5122200", "2510003", "Spesialsko, ortopedisk sydde sko, ombygg"),
    FOTSENGER("5122300", "2519000", "Fotsenger"),
    ALMINNELIG_FOTTOY("5122400", "2510009", "Alminnelig fottøy ved ulik fotstørrelse"),
    PARYKK("5120000", "2510005", "Parykk"),
    BRYSTPROTESE("5121200", "2510007", "Brystprotese"),
    OYEPROTESE("5121100", "2510008", "Øyeprotese"),
    ANSIKTSDEFEKTPROTESE("5121000", "2510006", "Ansiktsdefektprotese"),
    TILPASNINGSKURS_BLINDE("4160001", navn = "Tilpasningskurs blinde"),
    TILPASNINGSKURS_DOVE("4160002", navn = "Tilpasningskurs døve"),
    TILPASNINGSKURS_DOVBLINDE("4160003", navn = "Tilpasningskurs døvblinde"),
    FOLKEHOYSKOLE("4160005", navn = "Folkehøyskole"),
    GRUNNMONSTER_OG_SOM("4640000", navn = "Grunnmønster og søm"),
    REISEUTGIFTER("4666000", navn = "Reiseutgifter"),

    /** Eldre samlebetegnelse som dekket fotseng og ortopediske hjelpemidler. */
    FOTSENG_ORTOPEDISKE_HJELPEMIDLER("2510000", navn = "Fotseng / ortopediske hjelpemidler"),
    UKJENT("-1", navn = "Ukjent"),
    ;

    companion object {
        fun fraKode(kode: String?): InfotrygdKontonummer =
            entries.find { it.kode == kode || it.gammelKode == kode }?: InfotrygdKontonummer.UKJENT
    }
}
