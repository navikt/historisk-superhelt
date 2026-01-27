package no.nav.historisk.superhelt.sak

enum class StonadsType(val navn: String, val beskrivelse: String?= null) {
    PARYKK("Parykk/hodeplagg", "Dekker kostnader til parykk og hodeplagg"),
    ORTOPEDI("Ortopedi", "Støtte til ortopedi med utbetalinger. Fottøy, protese, ortose spesialsko"),
    ANSIKT_PROTESE("Ansiktdefektprotese", ),
    OYE_PROTESE("Øyeprotese", ),
    BRYSTPROTESE("Brystprotese/spesial-bh"),
    FOTTOY("Allminnelig fottøy i ulik størrelse"),
    REISEUTGIFTER("Reiseutgifter"),
//    FOLKEHOYSKOLE("Folkehøyskole/tilpassningskurs"),
//    GRUNNMONSTER("Grunnmønster"),

//    HUND("Førerhund/servicehund"),
//    FUNKSJONSASSISTENT("Funksjonsassistent"),
//    DATAHJELPEMIDDEL("Tilskudd datahjelpemiddel"),
//    BIL("Bil"),
//    REP_SPES_UTSTYR("Reparasjon spesialutstyr"),
//    TOLK("Tolk"),
}