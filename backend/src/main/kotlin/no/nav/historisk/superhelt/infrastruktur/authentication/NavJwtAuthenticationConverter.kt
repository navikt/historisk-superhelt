package no.nav.historisk.superhelt.infrastruktur.authentication

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component


const val rolePrefix = "ROLE_"
const val temaPrefix = "TEMA_"

@Component
 class NavJwtAuthenticationConverter(gruppeMapping: GruppeMapping ) : Converter<Jwt, JwtAuthenticationToken> {
    private val gruppeRoleMapping= gruppeMapping.roller
    private val gruppeTemaMapping= gruppeMapping.tema

    override fun convert(jwt: Jwt): JwtAuthenticationToken {
        val authorities = mapGroupsToAuthorities(jwt)
        val navIdent = extractNavIdent(jwt) ?: "ukjent"

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

        val tema= groupsClaims.map { gruppeTemaMapping[it] }
        tema.filterNotNull().forEach { tema ->
            authorities.add(SimpleGrantedAuthority("${temaPrefix}${tema.name}"))
        }

        return authorities
    }

    private fun extractNavIdent(jwt: Jwt): String? {
        return jwt.getClaimAsString("NAVident")
            ?: jwt.getClaimAsString("navident")
            ?: jwt.getClaimAsString(JwtClaimNames.SUB)
    }
}
