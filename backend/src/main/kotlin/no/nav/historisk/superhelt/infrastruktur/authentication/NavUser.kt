package no.nav.historisk.superhelt.infrastruktur.authentication

import jakarta.persistence.Embeddable
import no.nav.common.types.NavIdent

@Embeddable
data class NavUser(val navIdent: NavIdent, val navn: String){
    companion object {
        /** Markerer at verdien ikke er satt. Brukes for å sette til null i databasen ved oppdateringer */
        val NULL_VALUE = NavUser(NavIdent("NULL_VALUE"), "")
    }
}
