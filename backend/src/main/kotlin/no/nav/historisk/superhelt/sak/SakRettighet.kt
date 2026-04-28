package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.authentication.Permission
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
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

    FEILREGISTERE,
    HENLEGGE,

    /** Gir rettighet til å tilbakestille en feilaktig gjenåpnet sak til tilstanden fra siste vedtak */
    TILBAKESTILL_GJENAPNING,

    /** Gir rettighet til å sende klage til Kabal for en ferdigstilt sak */
    SEND_KLAGE,
    /** Sende fritekstbrev på en sak */
    FRITEKSTBREV


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
                    rettigheter.add(SakRettighet.FRITEKSTBREV)

                    if (gjenapnet) {
                        rettigheter.add(SakRettighet.TILBAKESTILL_GJENAPNING)
                    } else {
                        rettigheter.add(SakRettighet.HENLEGGE)
                        rettigheter.add(SakRettighet.FEILREGISTERE)
                    }
                }

            }

            SakStatus.TIL_ATTESTERING -> {
                if (hasRole(Role.ATTESTANT) && navIdent != saksbehandler.navIdent) {
                    rettigheter.add(SakRettighet.ATTESTERE)
                }
            }

            SakStatus.FERDIG_ATTESTERT -> {
                // TODO sette rettigheter som kan styre knapper for ferdigstilling
            }

            SakStatus.FERDIG -> {
                if (hasRole(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.GJENAPNE)
                    rettigheter.add(SakRettighet.SEND_KLAGE)
                    rettigheter.add(SakRettighet.FRITEKSTBREV)
                }
            }

            SakStatus.FEILREGISTRERT -> {
                // Det er ikke mulig å gjenåpne en feilregistrert sak
            }


        }
    }

    return rettigheter.toSet()
}
