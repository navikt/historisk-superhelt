package no.nav.historisk.superhelt.infrastruktur

import no.nav.historisk.superhelt.infrastruktur.authentication.AuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.authentication.SystemUserAuthenticationToken
import no.nav.historisk.superhelt.infrastruktur.authentication.authenticatedUser
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

const val rolePrefix = "ROLE_"

fun getCurrentUserRoles(): List<Role> {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication?.authorities
        ?.filter { it.authority?.startsWith(rolePrefix) ?: false }
        ?.map { it.authority?.removePrefix(rolePrefix) }
        ?.mapNotNull { Role.valueOf(it!!) }
        ?: emptyList()
}

fun hasRole(role: Role): Boolean {
    return getCurrentUserRoles().contains(role)
}

fun hasPermission(permission: Permission): Boolean {
    return getCurrentUserPermissions().contains(permission)
}

private val permissionStringValues = Permission.entries.map { it.name }

fun getCurrentUserPermissions(): List<Permission> {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication?.authorities
        ?.filter { permissionStringValues.contains(it.authority) }
        ?.map { Permission.valueOf(it.authority!!) }
        ?: emptyList()
}

fun getCurrentNavIdent() = getAuthenticatedUser().navIdent
fun getCurrentNavUser() = getAuthenticatedUser().navUser
fun getCurrentUserToken() = getCurrentJwt()?.tokenValue
fun getCurrentJwt() = getAuthenticatedUser().jwt

fun getAuthenticatedUser(): AuthenticatedUser {
    return when (val authentication = SecurityContextHolder.getContext().authentication) {
        is JwtAuthenticationToken -> authentication.authenticatedUser
        is SystemUserAuthenticationToken -> authentication.authenticatedUser
        else -> throw IllegalStateException("Unknown Authenticated user $authentication")
    }
}


