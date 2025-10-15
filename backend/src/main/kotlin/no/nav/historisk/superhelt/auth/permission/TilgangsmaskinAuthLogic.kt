package no.nav.historisk.superhelt.auth.permission

import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import no.nav.person.Fnr
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.stereotype.Component

@Component("tilgangsmaskin")
open class TilgangsmaskinAuthLogic(private val tilgangsmaskinService: TilgangsmaskinService) {

    open fun harTilgang(fnr: Fnr): TilgangsmaskinAuthorizationDecision {
        val (granted, response) = tilgangsmaskinService.sjekkKomplettTilgang(fnr)

        return TilgangsmaskinAuthorizationDecision(granted, response?.begrunnelse)
    }


    class TilgangsmaskinAuthorizationDecision(granted: Boolean, val reason: String? = null) :
        AuthorizationDecision(granted) {
        override fun toString(): String {
            return javaClass.getSimpleName() + " [" + "granted=" + isGranted() + ", reason=" + this.reason + ']'

        }
    }

}