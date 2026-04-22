package no.nav.historisk.superhelt.infrastruktur.authentication

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

object SecurityContextUtils {

    fun <T> runAsSystemuser(name: String?, permissions: List<Permission>, task: () -> T): T {
        val originalContext = SecurityContextHolder.getContext()
        return try {
            val permissionAuthorities = permissions.map { SimpleGrantedAuthority(it.name) }
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply {
                authentication = SystemUserAuthenticationToken(name= name, authorities = permissionAuthorities)
            })

            task()
        } finally {
            SecurityContextHolder.setContext(originalContext)
        }
    }
}
