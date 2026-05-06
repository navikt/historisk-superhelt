package no.nav.historisk.superhelt.test

import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.SystemUserAuthenticationToken
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
    val tema: Array<FellesKodeverkTema> = [FellesKodeverkTema.HEL, FellesKodeverkTema.HJE]
) {
    class Factory : WithSecurityContextFactory<WithSystemUser> {
        override fun createSecurityContext(annotation: WithSystemUser): SecurityContext {
            val context = SecurityContextHolder.createEmptyContext()
            val allAuthorities =
                mapAuthorities(roles = emptyList(), tema = annotation.tema.toList(), permissions = annotation.permissions.toList())

            val authentication = SystemUserAuthenticationToken(name = annotation.name, authorities = allAuthorities)
            context.authentication = authentication
            return context
        }
    }
}
