package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.Permission
import no.nav.historisk.superhelt.infrastruktur.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockJwtAuth.Factory::class)
annotation class WithMockJwtAuth(
    val navIdent: String = "test456",
    val name: String = "Test User",
    val roles: Array<Role> = [],
    val permissions: Array<Permission> = [Permission.READ],
    val claims: Array<String> = [] // Format: "key=value"
) {
    class Factory : WithSecurityContextFactory<WithMockJwtAuth> {
        override fun createSecurityContext(annotation: WithMockJwtAuth): SecurityContext {
            val context = SecurityContextHolder.createEmptyContext()

            val navIdent = if (annotation.navIdent.isEmpty()) "test456" else annotation.navIdent
            val name = if (annotation.name.isEmpty()) "Test User" else annotation.name

            val claimsMap = annotation.claims.associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }.toMutableMap()
            claimsMap["sub"] = navIdent
            claimsMap["name"] = name

            val jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claims { it.putAll(claimsMap) }
                .build()

            val roleAuthoroties = annotation.roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
            val permissionAuthorities = annotation.permissions.map { SimpleGrantedAuthority(it.name) }
            val allAuthorities = roleAuthoroties + permissionAuthorities
            val authentication = JwtAuthenticationToken(jwt, allAuthorities, navIdent)

            context.authentication = authentication
            return context
        }
    }
}