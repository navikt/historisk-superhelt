package no.nav.helved

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate

data class UtbetalingMelding(
    val id: String,
    val sakId: String,
    val behandlingId: String,
    val personident: String,
    val stønad: String,

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val vedtakstidspunkt: Instant,
    val periodetype: Periodetype,
    val perioder: List<Periode>,
    val saksbehandler: String,
    val beslutter: String
)

data class Periode(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fom: LocalDate,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val tom: LocalDate,
    val beløp: Int
)

enum class Periodetype {
    DAG
}
