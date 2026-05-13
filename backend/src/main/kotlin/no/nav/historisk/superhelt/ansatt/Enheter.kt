package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.Enhetsnummer
import no.nav.historisk.superhelt.StonadsType

object Enheter {
    private val navArbeidOgYtelser = Enhetsnummer("4485")
    private val navTiltakInnlandet = Enhetsnummer("0587")

    //    val hjelpemiddelsentralene = Enhetsnummer("47")
    private val dummyEnhet = Enhetsnummer("9999")

    fun guessEnhet(stonadsType: StonadsType): Enhetsnummer {
        if (stonadsType.tema == FellesKodeverkTema.HEL) {
            return navArbeidOgYtelser
        }
        return when (stonadsType) {
            StonadsType.ARBEID_UTDANNING -> navTiltakInnlandet
            else -> dummyEnhet
        }
    }
}
