package no.nav.historisk.superhelt.auth.permission

import no.nav.person.Fnr
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.stereotype.Component

@Component("tilgangsmaskin")
open class TilgangsmaskinAuthLogic {

    open fun harTilgang(fnr: Fnr): TilgangsmaskinAuthorizationDecision {
        val granted = !fnr.startsWith("9")
        val reason = if (granted) null else "Fnr starter med 9"
        return TilgangsmaskinAuthorizationDecision(granted, reason)
    }


    class TilgangsmaskinAuthorizationDecision(granted: Boolean, val reason: String? = null) :
        AuthorizationDecision(granted) {
        override fun toString(): String {
            return javaClass.getSimpleName() + " [" + "granted=" + isGranted() + ", reason=" + this.reason + ']'

        }
    }

}