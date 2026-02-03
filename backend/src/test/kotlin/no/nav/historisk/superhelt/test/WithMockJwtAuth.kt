package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
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
            val claimsMap = annotation.claims.associate {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
            }
            val authentication = mockedJwtAutenticationToken(
                claims = claimsMap,
                navIdent = annotation.navIdent,
                username = annotation.name,
                roles = annotation.roles.toList(),
                permissions = annotation.permissions.toList()
            )
            context.authentication = authentication
            return context
        }
    }
}