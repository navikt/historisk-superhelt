package no.nav.historisk.superhelt.sak

import no.nav.helved.KlasseKode

enum class StonadsType(val navn: String, val klassekode: KlasseKode, val beskrivelse: String? = null) {
    PARYKK(navn = "Parykk/hodeplagg", klassekode = KlasseKode.PARYKK, "Dekker kostnader til parykk og hodeplagg"),
    ANSIKT_PROTESE("Ansiktdefektprotese", KlasseKode.ANSIKTSDEFEKTPROTESE),
    OYE_PROTESE("Øyeprotese", KlasseKode.ØYEPROTESE),
    BRYSTPROTESE("Brystprotese/spesial-bh", KlasseKode.BRYSTPROTESE),
    FOTTOY("Alminnelig fottøy i ulik størrelse", KlasseKode.VANLIGE_SKO),
    REISEUTGIFTER("Reiseutgifter", KlasseKode.REISEUTGIFTER),
    FOTSENG("Fotseng", KlasseKode.FOTSENG),
    PROTESE("Ortopedisk protese", KlasseKode.ORTOPEDISK_PROTESE),
    ORTOSE("Ortopedisk ortose", KlasseKode.ORTOSE),
    SPESIALSKO("Ortopediske spesialsko", KlasseKode.SPESIALSKO),

//    FOLKEHOYSKOLE("Folkehøyskole/tilpassningskurs"),
//    GRUNNMONSTER("Grunnmønster"),

//    HUND("Førerhund/servicehund"),
//    FUNKSJONSASSISTENT("Funksjonsassistent"),
//    DATAHJELPEMIDDEL("Tilskudd datahjelpemiddel"),
//    BIL("Bil"),
//    REP_SPES_UTSTYR("Reparasjon spesialutstyr"),
//    TOLK("Tolk"),
}