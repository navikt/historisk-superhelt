package no.nav.historisk.superhelt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.consts.FellesKodeverkTema.HEL
import no.nav.common.consts.FellesKodeverkTema.HJE
import no.nav.common.types.Enhetsnummer
import no.nav.helved.KlasseKode
import no.nav.historisk.superhelt.ansatt.Enheter

enum class StonadsType(
    val navn: String,
    val klassekoder: List<KlasseKode>,
    val beskrivelse: String? = null,
    val tema: FellesKodeverkTema,
    val enhet: Enhetsnummer) {

    /* Helsetjenester */
    PARYKK(
        navn = "Parykk/hodeplagg",
        klassekoder = listOf(KlasseKode.PARYKK),
        beskrivelse = "Dekker kostnader til parykk og hodeplagg",
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    ANSIKT_PROTESE(
        navn = "Ansiktdefektprotese",
        klassekoder = listOf(KlasseKode.ANSIKTSDEFEKTPROTESE),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    OYE_PROTESE(
        navn = "Øyeprotese",
        klassekoder = listOf(KlasseKode.ØYEPROTESE),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    BRYSTPROTESE(
        navn = "Brystprotese/spesial-bh",
        klassekoder = listOf(KlasseKode.BRYSTPROTESE),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    FOTTOY(
        navn = "Alminnelig fottøy i ulik størrelse",
        klassekoder = listOf(KlasseKode.VANLIGE_SKO),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    REISEUTGIFTER(
        navn = "Reiseutgifter helsetjenester",
        klassekoder = listOf(KlasseKode.REISEUTGIFTER),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    FOTSENG(
        navn = "Fotseng",
        klassekoder = listOf(KlasseKode.FOTSENG),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    PROTESE(
        navn = "Ortopedisk protese",
        klassekoder = listOf(KlasseKode.ORTOPEDISK_PROTESE),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    ORTOSE(
        navn = "Ortopedisk ortose",
        klassekoder = listOf(KlasseKode.ORTOSE),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),
    SPESIALSKO(
        navn = "Ortopediske spesialsko",
        klassekoder = listOf(KlasseKode.SPESIALSKO),
        tema = HEL,
        enhet = Enheter.navArbeidOgYtelser
    ),

    ARBEID_UTDANNING(
        navn = "Arbeid og utdanningsreiser",
        tema = HJE,
        klassekoder = emptyList(),
        enhet = Enheter.navTiltakInnlandet
    )

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

