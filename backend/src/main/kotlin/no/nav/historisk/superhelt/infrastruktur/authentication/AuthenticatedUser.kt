package no.nav.historisk.superhelt.infrastruktur.authentication

import no.nav.common.types.NavIdent
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

private val permissionStringValues = Permission.entries.map { it.name }

data class AuthenticatedUser(
    val navIdent: NavIdent,
    val userName: String,
    val jwt: Jwt?,
    val systemUser: Boolean = false,
    val authorities: Collection<GrantedAuthority> = emptySet()
) {
    val navUser: NavUser
        get() = NavUser(
            navIdent = navIdent,
            navn = userName
        )

    val roles: List<Role>
        get() = authorities
            .filter { it.authority?.startsWith(rolePrefix) ?: false }
            .map { it.authority?.removePrefix(rolePrefix) }
            .mapNotNull { Role.valueOf(it!!) }

    val permissions: List<Permission>
        get() = authorities
            .filter { permissionStringValues.contains(it.authority) }
            .map { Permission.valueOf(it.authority!!) }

    fun hasRole(role: Role): Boolean {
        return roles.contains(role)
    }

    fun hasPermission(permission: Permission): Boolean {
        return permissions.contains(permission)
    }

}

val JwtAuthenticationToken.authenticatedUser: AuthenticatedUser
    get() = AuthenticatedUser(
        navIdent = this.name?.let { NavIdent(it) } ?: throw IllegalStateException("NavIdent ikke funnet i JWT"),
        userName = this.token.getClaimAsString("name")
            ?: this.token.getClaimAsString("given_name")
            ?: this.name,
        jwt = this.token,
        authorities = this.authorities
    )
