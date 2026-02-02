package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.Permission
import no.nav.historisk.superhelt.infrastruktur.Role
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.authentication.hasPermission
import no.nav.historisk.superhelt.infrastruktur.authentication.hasRole

enum class SakRettighet {
    LES,

    /** Gir rettighet til å endre sakens innhold og fatte vedtak*/
    SAKSBEHANDLE,

    /** Gir rettighet til å attestere og ferdigstille sak */
    ATTESTERE,

    /** Gir rettighet til å gjenåpne en ferdigstilt sak */
    GJENAPNE,
}

internal fun getRettigheter(sak: Sak): Set<SakRettighet> {
    val rettigheter = mutableSetOf<SakRettighet>()
    val navIdent = getAuthenticatedUser().navIdent

    if (hasPermission(Permission.READ)) {
        rettigheter.add(SakRettighet.LES)
    }
    with(sak) {
        when (status) {
            SakStatus.UNDER_BEHANDLING -> {
                if (hasRole(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.SAKSBEHANDLE)
                }
            }

            SakStatus.TIL_ATTESTERING -> {
                if (hasRole(Role.ATTESTANT) && navIdent != saksbehandler.navIdent) {
                    rettigheter.add(SakRettighet.ATTESTERE)
                }
            }

            SakStatus.FERDIG -> {
                if (hasRole(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.GJENAPNE)
                }
            }
        }
    }

    return rettigheter.toSet()
}
