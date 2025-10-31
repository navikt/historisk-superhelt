package no.nav.historisk.superhelt.sak.rest

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.sak.Forhandstilsagn
import no.nav.historisk.superhelt.sak.Utbetaling
import no.nav.person.Fnr
import java.time.LocalDate

enum class UtbetalingsType {
    BRUKER,
    FORHANDSTILSAGN,
    INGEN
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
    val utbetalingsType: UtbetalingsType? = null,
    val utbetaling: Utbetaling? = null,
    val forhandstilsagn: Forhandstilsagn? = null,
)


