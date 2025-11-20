package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.*

enum class SakRettighet {
    LES,

    /** Gir rettighet til å endre sakens innhold og fatte vedtak*/
    SAKSBEHANDLE,

    /** Gir rettighet til å attestere og ferdigstille sak */
    FERDIGSTILLE,

    /** Gir rettighet til å gjenåpne en ferdigstilt sak */
    GJENAPNE,
}

internal fun getRettigheter(sak: Sak): Set<SakRettighet> {
    val rettigheter = mutableSetOf<SakRettighet>()
    val navIdent = getCurrentNavIdent()

    if (hasPermission(Permission.READ)) {
        rettigheter.add(SakRettighet.LES)
    }
    with(sak) {
        when (status) {
            SakStatus.UNDER_BEHANDLING -> {
                if (hasRole(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.SAKSBEHANDLE)
                    //TODO Fjerne denne når vi har totrinnskontroll
                    rettigheter.add(SakRettighet.FERDIGSTILLE)
                }
            }

            SakStatus.TIL_ATTESTERING -> {
                if (hasRole(Role.ATTESTANT) && navIdent != saksbehandler) {
                    rettigheter.add(SakRettighet.FERDIGSTILLE)
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
