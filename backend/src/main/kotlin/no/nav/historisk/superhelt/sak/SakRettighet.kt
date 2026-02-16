package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.authentication.*

enum class SakRettighet {
    LES,

    /** Gir rettighet til å endre sakens innhold og fatte vedtak*/
    SAKSBEHANDLE,

    /** Gir rettighet til å attestere og ferdigstille sak */
    ATTESTERE,

    /** Gir rettighet til å gjenåpne en ferdigstilt sak */
    GJENAPNE,

    FEILREGISTERE,
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
                    // TODO ikke kunne feilregistere en gjenåpnet sak
                    rettigheter.add(SakRettighet.FEILREGISTERE)

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
                }
            }

            SakStatus.FEILREGISTRERT -> {
                // Det er ikke mulig å gjenåpne en feilregistrert sak
            }


        }
    }

    return rettigheter.toSet()
}
