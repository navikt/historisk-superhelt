package no.nav.historisk.superhelt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.consts.FellesKodeverkTema.HEL
import no.nav.helved.KlasseKode

enum class StonadsType(val navn: String, val klassekode: KlasseKode, val beskrivelse: String? = null, val tema: FellesKodeverkTema) {

    /* Helsetjenester */
    PARYKK(navn = "Parykk/hodeplagg", klassekode = KlasseKode.PARYKK, beskrivelse = "Dekker kostnader til parykk og hodeplagg", tema = HEL),
    ANSIKT_PROTESE(navn = "Ansiktdefektprotese", klassekode = KlasseKode.ANSIKTSDEFEKTPROTESE, tema = HEL),
    OYE_PROTESE(navn = "Øyeprotese", klassekode = KlasseKode.ØYEPROTESE, tema = HEL),
    BRYSTPROTESE(navn = "Brystprotese/spesial-bh", klassekode = KlasseKode.BRYSTPROTESE, tema = HEL),
    FOTTOY(navn = "Alminnelig fottøy i ulik størrelse", klassekode = KlasseKode.VANLIGE_SKO, tema = HEL),
    REISEUTGIFTER(navn = "Reiseutgifter helsetjenester", klassekode = KlasseKode.REISEUTGIFTER, tema = HEL),
    FOTSENG(navn = "Fotseng", klassekode = KlasseKode.FOTSENG, tema = HEL),
    PROTESE(navn = "Ortopedisk protese", klassekode = KlasseKode.ORTOPEDISK_PROTESE, tema = HEL),
    ORTOSE(navn = "Ortopedisk ortose", klassekode = KlasseKode.ORTOSE, tema = HEL),
    SPESIALSKO(navn = "Ortopediske spesialsko", klassekode = KlasseKode.SPESIALSKO, tema = HEL),

//    ARBEID_UTDANNING(navn = "Arbeid og utdanningsreiser", tema = HJE, klassekode = KlasseKode.ANSIKTSDEFEKTPROTESE)

    /* Hjelpemidler */
//    FOLKEHOYSKOLE("Folkehøyskole/tilpassningskurs"),
//    GRUNNMONSTER("Grunnmønster"),

//    HUND("Førerhund/servicehund"),
//    FUNKSJONSASSISTENT("Funksjonsassistent"),
//    DATAHJELPEMIDDEL("Tilskudd datahjelpemiddel"),
//    BIL("Bil"),
//    REP_SPES_UTSTYR("Reparasjon spesialutstyr"),
//    TOLK("Tolk"),
    ;

    val defaultKlasseKode: KlasseKode get() = klassekode


}
