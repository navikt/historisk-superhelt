package no.nav.historisk.superhelt.ansatt

import no.nav.common.consts.EksternFellesKodeverkTema
import no.nav.entraproxy.Enhet
import no.nav.entraproxy.EntraProxyClient
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getCurrentUserRoles
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

internal const val NAVANSATT_CACHE = "navansattCache"

/** Henter informasjon om den innloggede NAV-ansatte, inkludert roller, enheter og temaer.  Resultatet caches*/
@Service
class NavAnsattService(private val entraProxyClient: EntraProxyClient, cacheManager: CacheManager) {
    private val cache = cacheManager.getCache(NAVANSATT_CACHE)
        ?: throw IllegalStateException("Cache '${NAVANSATT_CACHE}' not found")


    private fun hentNavEnheter(): List<Enhet> {
        return entraProxyClient.hentEnheter()
    }

    private fun hentNavTema(): List<EksternFellesKodeverkTema> {
        return entraProxyClient.hentTema()
            .filter { EksternFellesKodeverkTema.hasItem(it) }
            .map { EksternFellesKodeverkTema.valueOf(it) }
    }

    private fun hentFraEntraProxy(): NavAnsatt {
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

    fun hentNavAnsatt(): NavAnsatt {
        val user = getAuthenticatedUser().navUser
        val cacheKey = "${user.navIdent}"

        return cache.get(cacheKey, NavAnsatt::class.java)
            ?: hentFraEntraProxy().also { result ->
                cache.put(cacheKey, result)
            }
    }

}
