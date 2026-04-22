package no.nav.historisk.superhelt.test

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SystemUserAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
/** Kjører testen som en systembruker */
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithSystemUser.Factory::class)
annotation class WithSystemUser(
    val name: String = "mocked system user",
    val permissions: Array<Permission> = [Permission.READ],
) {
    class Factory : WithSecurityContextFactory<WithSystemUser> {
        override fun createSecurityContext(annotation: WithSystemUser): SecurityContext {
            val context = SecurityContextHolder.createEmptyContext()
            val permissionAuthorities = annotation.permissions.map { SimpleGrantedAuthority(it.name) }

            val authentication = SystemUserAuthenticationToken(name = annotation.name, authorities = permissionAuthorities)
            context.authentication = authentication
            return context
        }
    }
}
