package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.EksternFellesKodeverkTema
import no.nav.entraproxy.Enhet
import no.nav.entraproxy.EntraProxyClient
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getCurrentUserRoles
import org.springframework.stereotype.Service

@Service
class NavAnsattService(private val entraProxyClient: EntraProxyClient) {


    fun hentNavEnheter(): Set<Enhet> {
        return entraProxyClient.hentEnheter().toSet()
    }

    fun hentNavTema(): Set<EksternFellesKodeverkTema> {
        return entraProxyClient.hentTema()
            .filter { EksternFellesKodeverkTema.hasItem(it) }
            .map { EksternFellesKodeverkTema.valueOf(it) }.toSet()
    }

    fun hentNavAnsatt(): NavAnsatt {
        val roles = getCurrentUserRoles()
        val user = getAuthenticatedUser().navUser
        return NavAnsatt(
            name = user.navn,
            ident = user.navIdent,
            roles = roles,
            enheter = hentNavEnheter(),
            tema = hentNavTema(),
        )
    }

}
