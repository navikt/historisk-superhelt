package no.nav.historisk.superhelt.test

//https://blog.aleksandar.io/spring-boot/kotlin/jwt/testing/jwt-testing-spring-kotlin/

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * Utfører en oppgave med en midlertidig bruker i SecurityContext.
 * Gjenoppretter den opprinnelige brukeren etter at oppgaven er utført.
 */
fun <T> withMockedUser(
    navIdent: String = "Z999999",
    username: String = "test user",
    permissions: List<Permission> = listOf(Permission.READ, Permission.WRITE),
    roles: List<Role> = emptyList(),
    claims: Map<String, String> = emptyMap(),
    task: () -> T
): T {
    val originalAuth = SecurityContextHolder.getContext().authentication

    try {
        val authentication = mockedJwtAutenticationToken(claims, navIdent, username, roles, permissions)
        SecurityContextHolder.getContext().authentication = authentication
        return task()
    } finally {
        SecurityContextHolder.getContext().authentication = originalAuth
    }
}

fun mockedJwtAutenticationToken(
    claims: Map<String, String>,
    navIdent: String,
    username: String,
    roles: List<Role>,
    permissions: List<Permission>): JwtAuthenticationToken {

    val claimsMap = claims.toMutableMap()
    claimsMap["sub"] = navIdent
    claimsMap["name"] = username

    val jwt = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claims { it.putAll(claimsMap) }
        .build()

    val roleAuthorities = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
    val permissionAuthorities = permissions.map { SimpleGrantedAuthority(it.name) }
    val allAuthorities = roleAuthorities + permissionAuthorities
    val authentication = JwtAuthenticationToken(jwt, allAuthorities, navIdent)
    return authentication
}
