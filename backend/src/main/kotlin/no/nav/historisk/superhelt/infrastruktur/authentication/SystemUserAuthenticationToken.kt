package no.nav.historisk.superhelt.infrastruktur.authentication

import no.nav.common.types.NavIdent
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/** Authentication token som representerer en systembruker (ikke en ekte bruker) */
class SystemUserAuthenticationToken(
    private val name: String?,
    authorities: Collection<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {

    init {
        isAuthenticated = true
    }

    override fun getCredentials() = "N/A"

    override fun getPrincipal() = name

    val authenticatedUser= AuthenticatedUser(
        navIdent = NavIdent(name?: "system"),
        userName = name?: "system",
        jwt = null
    )

}