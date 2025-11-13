package no.nav.historisk.superhelt.sak

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.historisk.superhelt.infrastruktur.*
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.rest.UtbetalingsType
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.person.Fnr
import java.time.Instant
import java.time.LocalDate

/** DTO */
data class Sak(
    val saksnummer: Saksnummer,
    val type: StonadsType,
    val fnr: Fnr,
    val tittel: String? = null,
    val soknadsDato: LocalDate? = null,
    val begrunnelse: String? = null,
    val status: SakStatus,
    val vedtak: VedtakType? = null,
    val opprettetDato: Instant,
    val saksbehandler: String,
    val utbetaling: Utbetaling? = null,
    val forhandstilsagn: Forhandstilsagn? = null,
) {
    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val utbetalingsType: UtbetalingsType
        get() =
            when {
                forhandstilsagn != null -> UtbetalingsType.FORHANDSTILSAGN
                utbetaling != null -> UtbetalingsType.BRUKER
                else -> UtbetalingsType.INGEN
            }

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val maskertPersonIdent: MaskertPersonIdent
        get() = fnr.toMaskertPersonIdent()

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonGetter
    fun rettigheter(): List<SakRettighet> {
        val rettigheter = mutableListOf<SakRettighet>()
        val permissions = getCurrentUserPermissions()
        val roller = getCurrentUserRoles()
        val navIdent = getCurrentNavIdent()

        if (permissions.contains(Permission.READ)) {
            rettigheter.add(SakRettighet.LES)
        }
        when (status) {
            SakStatus.UNDER_BEHANDLING -> {
                if (roller.contains(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.SAKSBEHANDLE)
                }
            }

            SakStatus.TIL_ATTESTERING -> {
                if (roller.contains(Role.ATTESTANT) && navIdent != saksbehandler) {
                    rettigheter.add(SakRettighet.ATTESTERE)
                }
            }

            SakStatus.FERDIG -> {
                if (roller.contains(Role.SAKSBEHANDLER)) {
                    rettigheter.add(SakRettighet.GJENAPNE)
                }
            }
        }

        return rettigheter.toList()
    }

    fun hasRettighet(rettighet: SakRettighet): Boolean {
        return rettigheter().contains(rettighet)
    }

}
