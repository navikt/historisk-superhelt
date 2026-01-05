package no.nav.historisk.superhelt.infrastruktur

import no.nav.common.types.NavIdent
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

const val rolePrefix = "ROLE_"

fun getCurrentUserRoles(): List<Role> {
    val authentication = getJwtAuthentication()
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
    val authentication = getJwtAuthentication()
    return authentication?.authorities
        ?.filter { permissionStringValues.contains(it.authority) }
        ?.map { Permission.valueOf(it.authority!!) }
        ?: emptyList()
}

fun getCurrentNavIdent(): NavIdent {
    val authentication = getJwtAuthentication()
    return authentication?.name?.let { NavIdent(it) } ?: throw IllegalStateException("NavIdent ikke funnet i JWT")
}

fun getCurrentNavUser() = NavUser(
    navIdent = getCurrentNavIdent(),
    navn = getCurrentUserName()
)


fun getCurrentUserToken(): String? {
    val authentication = getJwtAuthentication()
    return authentication?.token?.tokenValue
}

fun getCurrentUserName(): String {
    val jwt = getCurrentJwt()
    return jwt?.getClaimAsString("name")
        ?: jwt?.getClaimAsString("given_name")
        ?: getCurrentNavIdent().value
}

fun getCurrentJwt(): Jwt? {
    val authentication = getJwtAuthentication()
    return authentication?.token
}


private fun getJwtAuthentication(): JwtAuthenticationToken? {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication as? JwtAuthenticationToken
}

