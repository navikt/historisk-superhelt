package no.nav.historisk.superhelt.sak


enum class SakStatus {
    UNDER_BEHANDLING,
    TIL_ATTESTERING,
    FERDIG_ATTESTERT,
    FERDIG,
    FEILREGISTRERT;

     fun isFinal(): Boolean {
        return this == SakStatus.FERDIG || this == SakStatus.FEILREGISTRERT
    }


}