package no.nav.historisk.superhelt.sak

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Fnr
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import java.time.Instant
import java.time.LocalDate

/**
 * Representerer en sak for en person (fnr) innen en bestemt stønads type
 *
 * En åpen sak representerer en pågående behandling. Når saken ferdigstilles lages det ett vedtak.
 * Ved gjenåpning av en sak opprettes en ny behandling på samme saksnummer med ett nytt behandlingsNummer.
 *
 */
data class Sak(
    /** Saksnummer for å skille mellom ulike saker for samme person Unik */
    val saksnummer: Saksnummer,

    /** Generert behandlingsnummer for denne behandlingen av saken. Unik */
    val behandlingsnummer: Behandlingsnummer,

    val type: StonadsType,
    val fnr: Fnr,
    val status: SakStatus,

    val tittel: String? = null,

    val soknadsDato: LocalDate? = null,
    val tildelingsAar: String? = null,

    val begrunnelse: String? = null,

    val vedtaksResultat: VedtaksResultat? = null,

    val opprettetDato: Instant,
    val saksbehandler: NavIdent,
    val attestant: NavIdent? = null,

    val utbetaling: Utbetaling? = null,
    val forhandstilsagn: Forhandstilsagn? = null,
) {

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val utbetalingsType: UtbetalingsType
        get() = when {
            forhandstilsagn != null -> UtbetalingsType.FORHANDSTILSAGN
            utbetaling != null -> UtbetalingsType.BRUKER
            else -> UtbetalingsType.INGEN
        }

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val maskertPersonIdent: MaskertPersonIdent
        get() = fnr.toMaskertPersonIdent()

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val rettigheter: Set<SakRettighet>
        get() = getRettigheter(this)

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val tilstand: SakTilstand
        get() = SakTilstand(this)

}

