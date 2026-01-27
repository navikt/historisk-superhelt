package no.nav.historisk.superhelt.infrastruktur.permission

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component

@Component("tilgangsmaskin")
class TilgangsmaskinAuthLogic(private val tilgangsmaskinService: TilgangsmaskinService) {

    fun harTilgang(fnr: String?): Boolean? {
        if (fnr == null || fnr.isBlank()) {
            return null
        }
        val (granted, response) = tilgangsmaskinService.sjekkKomplettTilgang(FolkeregisterIdent(fnr))

//         return TilgangsmaskinAuthorizationDecision(granted, response?.begrunnelse)
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

