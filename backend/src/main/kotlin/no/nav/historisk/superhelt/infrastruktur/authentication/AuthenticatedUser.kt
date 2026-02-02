package no.nav.historisk.superhelt.infrastruktur.authentication

import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.infrastruktur.NavUser
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

data class AuthenticatedUser(
    val navIdent: NavIdent,
    val userName: String,
    val jwt: Jwt?
){
    val navUser: NavUser = NavUser(
        navIdent = navIdent,
        navn = userName
    )
    val userToken: String? = jwt?.tokenValue
}

val JwtAuthenticationToken.authenticatedUser: AuthenticatedUser
    get() =  AuthenticatedUser(
        navIdent =  this.name?.let { NavIdent(it) } ?: throw IllegalStateException("NavIdent ikke funnet i JWT"),
        userName = this.token.getClaimAsString("name")
            ?: this.token.getClaimAsString("given_name")
            ?: this.name,
        jwt = this.token
    )
