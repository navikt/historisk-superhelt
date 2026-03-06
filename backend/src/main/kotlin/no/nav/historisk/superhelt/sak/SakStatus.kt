package no.nav.historisk.superhelt.sak


enum class SakStatus(val navn: String) {
    UNDER_BEHANDLING("Under behandling"),
    TIL_ATTESTERING("Til attestering"),
    FERDIG_ATTESTERT("Ferdig attestert"),
    FERDIG("Ferdig"),
    FEILREGISTRERT("Feilregistrert");

     fun isFinal(): Boolean {
        return this == SakStatus.FERDIG || this == SakStatus.FEILREGISTRERT
    }


}