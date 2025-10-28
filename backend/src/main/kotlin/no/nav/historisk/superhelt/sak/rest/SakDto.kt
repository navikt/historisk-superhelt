package no.nav.historisk.superhelt.sak.rest

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.Forhandstilsagn
import no.nav.person.Fnr
import java.time.LocalDate

enum class UtbetalingsType {
    BRUKER,
    FORHANDSTILSAGN,
    INGEN
}

data class SakDto(
    val saksnummer: Saksnummer,
    val type: StonadsType,
    val fnr: Fnr,
    val maskertPersonIdent: MaskertPersonIdent,
    val tittel: String?,
    val begrunnelse: String?,
    val status: SakStatus,
    val vedtak: VedtakType?,
    val opprettetDato: LocalDate,
    val soknadsDato: LocalDate?,
    val saksbehandler: String,
    val utbetaling: UtbetalingDto? = null,
    val forhandstilsagn: Boolean? = null
) {

    val utbetalingsType: UtbetalingsType
        get() = when {
            forhandstilsagn == true -> UtbetalingsType.FORHANDSTILSAGN
            utbetaling != null -> UtbetalingsType.BRUKER
            else -> UtbetalingsType.INGEN

        }

}

data class SakCreateRequestDto(
    val type: StonadsType,
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "[0-9]*", message = "Fødselsnummer må kun inneholde tall")
    val fnr: Fnr,
    val tittel: String? = null,
    val soknadsDato: LocalDate? = null,
)

data class SakUpdateRequestDto(
    val type: StonadsType? = null,
    val tittel: String? = null,
    val begrunnelse: String? = null,
    val soknadsDato: LocalDate? = null,
    val vedtak: VedtakType? = null,
    val belop: Double? = null,
    val utbetalingsType: UtbetalingsType? = null
)

fun Sak.toResponseDto() = SakDto(
    saksnummer = this.saksnummer ?: Saksnummer("Ukjent"),
    type = this.type,
    fnr = this.fnr,
    maskertPersonIdent = this.fnr.toMaskertPersonIdent(),
    tittel = this.tittel,
    begrunnelse = this.begrunnelse,
    status = this.status,
    opprettetDato = this.opprettetDato,
    saksbehandler = this.saksbehandler,
    soknadsDato = this.soknadsDato,
    vedtak = this.vedtak,
    utbetaling = this.utbetaling.toResponseDto(),
    forhandstilsagn = this.forhandstilsagn != null
)


