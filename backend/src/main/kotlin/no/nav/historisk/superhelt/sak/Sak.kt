package no.nav.historisk.superhelt.sak

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
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
    val status: SakStatus,

    @field:NotBlank(message = "Sakstittel må være satt")
    @field:Size(max = 200, message = "Sakstittel kan ikke være lengre enn {max} tegn")
    val tittel: String? = null,

    @field:NotNull(message = "Søknadsdato må være satt")
    val soknadsDato: LocalDate? = null,

    @field:Size(max = 1000, message = "Begrunnelse kan ikke være lengre enn {max} tegn")
    val begrunnelse: String? = null,

    @field:NotNull(message = "Vedtak må være satt")
    val vedtak: VedtakType? = null,

    val opprettetDato: Instant,
    val saksbehandler: String,

    @field:Valid
    val utbetaling: Utbetaling? = null,
    @field:Valid
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

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val rettigheter: Set<SakRettighet>
        get() = getRettigheter(this)

    @get:JsonProperty(access = JsonProperty.Access.READ_ONLY)
    val tilstand: SakTilstand
        get() = SakTilstand(this)


}
