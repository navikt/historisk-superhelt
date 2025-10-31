package no.nav.historisk.superhelt.test

//https://blog.aleksandar.io/spring-boot/kotlin/jwt/testing/jwt-testing-spring-kotlin/

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User

/**
 * Utfører en oppgave med en midlertidig bruker i SecurityContext.
 * Gjenoppretter den opprinnelige brukeren etter at oppgaven er utført.
 */
fun <T> withMockedUser(
    username: String = "Z999999",
    authorities: List<String> = listOf("READ", "WRITE"),
    block: () -> T
): T {
    val originalAuth = SecurityContextHolder.getContext().authentication

    try {
        val grantedAuthorities = authorities.map { authority -> SimpleGrantedAuthority(authority) }
        val principal = User(username, "", true, true, true, true, grantedAuthorities)
        val authentication: Authentication = UsernamePasswordAuthenticationToken.authenticated(
            principal,
            principal.getPassword(), principal.getAuthorities()
        )
        SecurityContextHolder.getContext().authentication = authentication

        return block()
    } finally {
        SecurityContextHolder.getContext().authentication = originalAuth
    }
}
