package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.Enhetsnummer
import no.nav.historisk.superhelt.StonadsType

object Enheter {
    val navArbeidOgYtelser = Enhetsnummer("4485")
    val navTiltakInnlandet = Enhetsnummer("0587")

    //    val hjelpemiddelsentralene = Enhetsnummer("47")
    val dummyEnhet = Enhetsnummer("9999")

    fun guessEnhet(stonadsType: StonadsType): Enhetsnummer {
        if (stonadsType.tema == FellesKodeverkTema.HEL) {
            return navArbeidOgYtelser
        }
        when (stonadsType) {
            StonadsType.ARBEID_UTDANNING -> return navTiltakInnlandet
            else -> return dummyEnhet
        }
    }
}
