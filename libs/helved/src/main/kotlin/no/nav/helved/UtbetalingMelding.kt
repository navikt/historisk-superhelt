package no.nav.helved

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate

// key i kafka er key på tilbakemedlinger
data class UtbetalingMelding(
    val id: String, // unik id for utbetalingmelding, brukes som transaksjonsid hos helved
    val sakId: String,
    val behandlingId: String, // alla journalpost
    val personident: String,
    val stønad: KlasseKode, // klassekode enum koordineres med oppdrag /helved

    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    val vedtakstidspunkt: Instant,
    val periodetype: Periodetype = Periodetype.EN_GANG,
    val perioder: List<Periode>,
    val saksbehandler: String,
    val beslutter: String
)

// Samme dag som det utbetales
data class Periode(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fom: LocalDate,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val tom: LocalDate,
    val beløp: Int
)

enum class Periodetype {
    EN_GANG
}
