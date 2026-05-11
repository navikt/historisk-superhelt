package no.nav.historisk.superhelt.infrastruktur.authentication

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

fun getCurrentUserRoles(): List<Role> {
    return getAuthenticatedUserNullable()?.roles ?: emptyList()
}

fun getAuthenticatedUser(): AuthenticatedUser {
    return getAuthenticatedUserNullable()
        ?: throw IllegalStateException("Unknown Authenticated user ${SecurityContextHolder.getContext().authentication}")
}

private fun getAuthenticatedUserNullable(): AuthenticatedUser? {
    return when (val authentication = SecurityContextHolder.getContext().authentication) {
        is JwtAuthenticationToken -> authentication.authenticatedUser
        is SystemUserAuthenticationToken -> authentication.authenticatedUser
        else -> null
    }
}

fun isAuthenticated(): Boolean = getAuthenticatedUserNullable() != null
