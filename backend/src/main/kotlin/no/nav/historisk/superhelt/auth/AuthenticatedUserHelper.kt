package no.nav.historisk.superhelt.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

val rolePrefix = "ROLE_"

fun getCurrentUserRoles(): List<Role> {
    val authentication = getJwtAuthentication()
    return authentication?.authorities
        ?.filter { it.authority.startsWith(rolePrefix) }
        ?.map { it.authority.removePrefix(rolePrefix) }
        ?.mapNotNull { Role.valueOf(it) } ?: emptyList()

}

fun getCurrentNavIdent(): String? {
    val authentication = getJwtAuthentication()
    return authentication?.name
}

fun getCurrentUserToken(): String? {
    val authentication = getJwtAuthentication()
    return authentication?.token?.tokenValue
}

fun getCurrentUserName(): String? {
    val jwt = getCurrentJwt()
    return jwt?.getClaimAsString("name")
        ?: jwt?.getClaimAsString("given_name")
        ?: getCurrentNavIdent()
}

fun getCurrentJwt(): Jwt? {
    val authentication = getJwtAuthentication()
    return authentication?.token
}


private fun getJwtAuthentication(): JwtAuthenticationToken? {
    val authentication = SecurityContextHolder.getContext().authentication
    return authentication as? JwtAuthenticationToken
}

