package no.nav.historisk.superhelt.infrastruktur.authentication

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SecurityContextUtils {

    fun <T> runWithPermissions(permissions: List<Permission>, task: () -> T): T {
        val originalContext = SecurityContextHolder.getContext()
        return try {
            val jwt = getAuthenticatedUser().jwt ?: error("Ingen JWT funnet i sikkerhetskontekst")
            val roles = getCurrentUserRoles()

            val roleAuthorities = roles.map { SimpleGrantedAuthority("ROLE_${it.name}") }
            val permissionAuthorities = permissions.map { SimpleGrantedAuthority(it.name) }
            val allAuthorities = roleAuthorities + permissionAuthorities

            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply {
                authentication = JwtAuthenticationToken(jwt, allAuthorities, originalContext.authentication?.name)
            })

             task()
        } finally {
            SecurityContextHolder.setContext(originalContext)
        }
    }

    fun <T> runAsSystemuser(permissions: List<Permission>, task: () -> T): T {
        val originalContext = SecurityContextHolder.getContext()
        return try {
            val permissionAuthorities = permissions.map { SimpleGrantedAuthority(it.name) }
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext().apply {
                authentication = SystemUserAuthenticationToken(authorities = permissionAuthorities)
            })

            task()
        } finally {
            SecurityContextHolder.setContext(originalContext)
        }
    }
}