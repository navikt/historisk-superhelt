package no.nav.historisk.superhelt.infrastruktur.authentication

import no.nav.common.consts.FellesKodeverkTema

data class GruppeMapping(
    val roller: Map<String, Role>,
    val tema: Map<String, FellesKodeverkTema>,
)
