package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.EksternFellesKodeverkTema
import no.nav.common.types.NavIdent
import no.nav.entraproxy.Enhet
import no.nav.historisk.superhelt.infrastruktur.authentication.Role

data class NavAnsatt(
    val name: String,
    val ident: NavIdent,
    val roles: List<Role>,
    val enheter: List<Enhet>,
    val tema: List<EksternFellesKodeverkTema>

)
