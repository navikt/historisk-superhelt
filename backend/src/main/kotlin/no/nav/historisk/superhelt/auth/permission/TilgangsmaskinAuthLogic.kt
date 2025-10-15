package no.nav.historisk.superhelt.auth.permission

import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.person.Fnr
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component

@Component("tilgangsmaskin")
 class TilgangsmaskinAuthLogic(private val tilgangsmaskinService: TilgangsmaskinService) {

     fun harTilgang(fnr: Fnr?): Boolean? {
        if (fnr == null || fnr.isBlank()) {
            return null
        }
        val (granted, response) = tilgangsmaskinService.sjekkKomplettTilgang(fnr)

        if (!granted) {
            throw AuthorizationDeniedException(
                "Mangler tilgang til bruker",
                TilgangsmaskinAuthorizationDecision(false, response?.begrunnelse),
            )
        }
        return true

    }

    class TilgangsmaskinAuthorizationDecision(granted: Boolean, val reason: String? = null) :
        AuthorizationDecision(granted) {
        override fun toString(): String {
            return "Tilgangsmaskin nekter tilgang fordi " + this.reason

        }
    }

}

