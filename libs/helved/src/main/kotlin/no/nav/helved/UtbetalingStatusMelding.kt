package no.nav.helved

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class UtbetalingStatusMelding(
    val status: StatusType,
    val detaljer: Detaljer?= null,
    val error: StatusError? = null
)

/** Status fra hel ved.
 *
 * Opptrer som en slags tilstandsmaskin
 *
 * MOTTATT -> HOS_OPPDRAG -> OK / FEILET
 * */
enum class StatusType {
    /** 3 endelig Ferdig*/
    OK,

    /** endelig Valideringsfeil, ett eller annet  kan oppstå når som helst i prosessen*/
    FEILET,

    /** 1 hos hel ved*/
    MOTTATT,

    /** 2 Sendt til oppdrag*/
    HOS_OPPDRAG
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

data class StatusError(
    val statusCode: Int,
    val msg: String,
    val doc: String
) {

}