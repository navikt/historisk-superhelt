package no.nav.historisk.superhelt.infrastruktur

import jakarta.persistence.Embeddable
import no.nav.common.types.NavIdent

@Embeddable
data class NavUser(val navIdent: NavIdent, val navn: String)
