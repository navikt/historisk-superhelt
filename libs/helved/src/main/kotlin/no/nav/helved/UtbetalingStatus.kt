package no.nav.helved

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class UtbetalingStatus(
    val status: StatusType,
    val detaljer: Detaljer?,
    val error: StatusError?
)

enum class StatusType {
    OK, FEILET, MOTTATT, HOS_OPPDRAG
}

data class Detaljer(
    val ytelse: String, // HISTORISK
    val linjer: List<Linje>
)


data class Linje(
    val behandlingId: String,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fom: LocalDate,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val tom: LocalDate,
    val vedtakssats: Int,
    val beløp: Int,
    val klassekode: String
)

class StatusError(
    val statusCode: Int,
    val msg: String,
    val doc: String) {

}