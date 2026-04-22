package no.nav.historisk.superhelt

import no.nav.helved.KlasseKode

enum class StonadsType(val navn: String, val klassekode: KlasseKode, val beskrivelse: String? = null, tema: Tema) {

    /* Helsetjenester */
    PARYKK(navn = "Parykk/hodeplagg", klassekode = KlasseKode.PARYKK, beskrivelse = "Dekker kostnader til parykk og hodeplagg", tema = Tema.HEL),
    ANSIKT_PROTESE(navn = "Ansiktdefektprotese", klassekode = KlasseKode.ANSIKTSDEFEKTPROTESE, tema = Tema.HEL),
    OYE_PROTESE(navn = "Øyeprotese", klassekode = KlasseKode.ØYEPROTESE, tema = Tema.HEL),
    BRYSTPROTESE(navn = "Brystprotese/spesial-bh", klassekode = KlasseKode.BRYSTPROTESE, tema = Tema.HEL),
    FOTTOY(navn = "Alminnelig fottøy i ulik størrelse", klassekode = KlasseKode.VANLIGE_SKO, tema = Tema.HEL),
    REISEUTGIFTER(navn = "Reiseutgifter helsetjenester", klassekode = KlasseKode.REISEUTGIFTER, tema = Tema.HEL),
    FOTSENG(navn = "Fotseng", klassekode = KlasseKode.FOTSENG, tema = Tema.HEL),
    PROTESE(navn = "Ortopedisk protese", klassekode = KlasseKode.ORTOPEDISK_PROTESE, tema = Tema.HEL),
    ORTOSE(navn = "Ortopedisk ortose", klassekode = KlasseKode.ORTOSE, tema = Tema.HEL),
    SPESIALSKO(navn = "Ortopediske spesialsko", klassekode = KlasseKode.SPESIALSKO, tema = Tema.HEL),

    /* Hjelpemidler */
//    FOLKEHOYSKOLE("Folkehøyskole/tilpassningskurs"),
//    GRUNNMONSTER("Grunnmønster"),

//    HUND("Førerhund/servicehund"),
//    FUNKSJONSASSISTENT("Funksjonsassistent"),
//    DATAHJELPEMIDDEL("Tilskudd datahjelpemiddel"),
//    BIL("Bil"),
//    REP_SPES_UTSTYR("Reparasjon spesialutstyr"),
//    TOLK("Tolk"),
}
