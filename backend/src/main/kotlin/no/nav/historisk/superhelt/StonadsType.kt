package no.nav.historisk.superhelt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.consts.FellesKodeverkTema.HEL
import no.nav.common.consts.FellesKodeverkTema.HJE
import no.nav.helved.KlasseKode

enum class StonadsType(val navn: String, val klassekoder: List<KlasseKode>, val beskrivelse: String? = null, val tema: FellesKodeverkTema) {

    /* Helsetjenester */
    PARYKK(
        navn = "Parykk/hodeplagg",
        klassekoder = listOf(KlasseKode.PARYKK),
        beskrivelse = "Dekker kostnader til parykk og hodeplagg",
        tema = HEL
    ),
    ANSIKT_PROTESE(navn = "Ansiktdefektprotese", klassekoder = listOf(KlasseKode.ANSIKTSDEFEKTPROTESE), tema = HEL),
    OYE_PROTESE(navn = "Øyeprotese", klassekoder = listOf(KlasseKode.ØYEPROTESE), tema = HEL),
    BRYSTPROTESE(navn = "Brystprotese/spesial-bh", klassekoder = listOf(KlasseKode.BRYSTPROTESE), tema = HEL),
    FOTTOY(navn = "Alminnelig fottøy i ulik størrelse", klassekoder = listOf(KlasseKode.VANLIGE_SKO), tema = HEL),
    REISEUTGIFTER(navn = "Reiseutgifter helsetjenester", klassekoder = listOf(KlasseKode.REISEUTGIFTER), tema = HEL),
    FOTSENG(navn = "Fotseng", klassekoder = listOf(KlasseKode.FOTSENG), tema = HEL),
    PROTESE(navn = "Ortopedisk protese", klassekoder = listOf(KlasseKode.ORTOPEDISK_PROTESE), tema = HEL),
    ORTOSE(navn = "Ortopedisk ortose", klassekoder = listOf(KlasseKode.ORTOSE), tema = HEL),
    SPESIALSKO(navn = "Ortopediske spesialsko", klassekoder = listOf(KlasseKode.SPESIALSKO), tema = HEL),

    ARBEID_UTDANNING(navn = "Arbeid og utdanningsreiser", tema = HJE, klassekoder = emptyList())

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

    val defaultKlasseKode: KlasseKode? get() = klassekoder.firstOrNull()
    val kanUtbetales: Boolean get() = klassekoder.isNotEmpty()

}
