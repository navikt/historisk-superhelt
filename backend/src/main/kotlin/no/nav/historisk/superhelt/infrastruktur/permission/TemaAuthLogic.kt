package no.nav.historisk.superhelt.infrastruktur.permission

import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component

/**
 * 
 */
@Component("temaAuth")
class TemaAuthLogic{

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun harTilgang(tema: FellesKodeverkTema): Boolean? {

        if (getAuthenticatedUser().systemUser) {
            logger.trace("Dropper sjekk for systembruker")
            return true
        }

        if (!getAuthenticatedUser().hasTemaAccess(tema)) {
            throw AuthorizationDeniedException(
                "Mangler tilgang til tema",
                SakAuthorizationDecision(false, "Mangler tilgang til tema $tema. Bruker har tilgang til tema ${getAuthenticatedUser().tema.joinToString("), ")}")
            )
        }
        return true
    }

    class SakAuthorizationDecision(granted: Boolean, val reason: String) :
        AuthorizationDecision(granted) {
        override fun toString(): String {
            return reason

        }
    }
}
