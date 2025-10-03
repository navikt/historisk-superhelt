package no.nav.historisk.superhelt.auth

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class NavJwtAuthenticationConverter(private val gruppeMapping: Map<String, Gruppe> ) : Converter<Jwt, JwtAuthenticationToken> {


    override fun convert(jwt: Jwt): JwtAuthenticationToken {
        val authorities = extractAuthorities(jwt)
        val navIdent = extractNavIdent(jwt)

        return JwtAuthenticationToken(jwt, authorities, navIdent)
    }

    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val groupsClaims = jwt.getClaimAsStringList("groups") ?: emptyList()
        return groupsClaims
            .map { gruppeMapping[it] }
            .map { SimpleGrantedAuthority("ROLE_$it") }
    }

    private fun extractNavIdent(jwt: Jwt): String? {
        return jwt.getClaimAsString("NAVident")
            ?: jwt.getClaimAsString("navident")
            ?: jwt.getClaimAsString(JwtClaimNames.SUB)
    }
}
