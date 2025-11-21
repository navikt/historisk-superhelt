package no.nav.historisk.superhelt.sak.rest

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.common.types.Fnr
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.sak.VedtakType
import java.time.LocalDate

enum class UtbetalingsType {
    BRUKER,
    FORHANDSTILSAGN,
    INGEN,
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
)

data class UtbetalingRequestDto(val utbetalingsType: UtbetalingsType, val belop: Int?)
