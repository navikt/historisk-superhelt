package no.nav.historisk.superhelt.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken


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

