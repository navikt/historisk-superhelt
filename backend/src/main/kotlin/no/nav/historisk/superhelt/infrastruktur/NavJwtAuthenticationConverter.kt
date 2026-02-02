package no.nav.historisk.superhelt.infrastruktur

import no.nav.historisk.superhelt.infrastruktur.authentication.rolePrefix
import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class NavJwtAuthenticationConverter(private val gruppeRoleMapping: Map<String, Role> ) : Converter<Jwt, JwtAuthenticationToken> {

    override fun convert(jwt: Jwt): JwtAuthenticationToken {
        val authorities = mapGroupsToAuthorities(jwt)
        val navIdent = extractNavIdent(jwt)

        return JwtAuthenticationToken(jwt, authorities, navIdent)
    }

    private fun mapGroupsToAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val groupsClaims = jwt.getClaimAsStringList("groups") ?: emptyList()
        val roles = groupsClaims.map { gruppeRoleMapping[it] }
        val authorities = mutableSetOf<GrantedAuthority>()

        roles.filterNotNull().forEach { role ->
            authorities.add(SimpleGrantedAuthority("${rolePrefix}${role.name}"))
            authorities.addAll(role.permissions.map { SimpleGrantedAuthority(it.name) })
        }
        return authorities
    }

    private fun extractNavIdent(jwt: Jwt): String? {
        return jwt.getClaimAsString("NAVident")
            ?: jwt.getClaimAsString("navident")
            ?: jwt.getClaimAsString(JwtClaimNames.SUB)
    }
}
