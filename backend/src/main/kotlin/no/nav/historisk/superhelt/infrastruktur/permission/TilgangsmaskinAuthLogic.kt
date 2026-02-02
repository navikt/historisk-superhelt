package no.nav.historisk.superhelt.infrastruktur.permission

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.Permission
import no.nav.historisk.superhelt.infrastruktur.hasPermission
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.slf4j.LoggerFactory
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.stereotype.Component

@Component("tilgangsmaskin")
class TilgangsmaskinAuthLogic(private val tilgangsmaskinService: TilgangsmaskinService) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun harTilgang(fnr: String?): Boolean? {
        if (fnr.isNullOrBlank()) {
            logger.debug("Fnr er null eller blank, hopper over tilgangssjekk i tilgangsmaskin")
            return null
        }
        if (hasPermission(Permission.IGNORE_TILGANGSMASKIN)) {
            logger.debug("Bruker har IGNORE_TILGANGSMASKIN permission, hopper over tilgangssjekk i tilgangsmaskin")
            return true
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

