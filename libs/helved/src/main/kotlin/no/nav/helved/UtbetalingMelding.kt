package no.nav.helved

import java.time.Instant
import java.time.LocalDate

data class UtbetalingMelding(
    val id: String,
    val sakId: String,
    val behandlingId: String,
    val personident: String,
    val stønad: String,
    val vedtakstidspunkt: Instant,
    val periodetype: Periodetype,
    val perioder: List<Periode>,
    val saksbehandler: String,
    val beslutter: String
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate,
    val beløp: Int
)

enum class Periodetype {
    DAG
}
