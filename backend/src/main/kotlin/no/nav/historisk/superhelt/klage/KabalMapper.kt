package no.nav.historisk.superhelt.klage

import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.StonadsType
import no.nav.kabal.model.KabalYtelse

val StonadsType.kabalYtelse: KabalYtelse
    get() =
        when {
            this == StonadsType.ARBEID_UTDANNING -> KabalYtelse.HJE_AUR
            this.tema == FellesKodeverkTema.HEL -> KabalYtelse.HEL_HEL
            this.tema == FellesKodeverkTema.HJE -> KabalYtelse.HJE_HJE
            else -> {
                throw IllegalArgumentException("Ukjent Kabal-ytelse for stonadstype ${this.navn}")
            }
        }
