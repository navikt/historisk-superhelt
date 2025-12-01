package no.nav.historisk.pdfgen

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.common.types.Behandlingsnummer
import no.nav.common.types.Fnr
import java.time.LocalDate


data class PdfgenRequest(
    val behandlingsnummer: Behandlingsnummer,
    val personalia: Personalia,
    @get:JsonFormat(pattern = "dd.MM.yyyy")
    val datoForUtsending: LocalDate,
    val saksbehandlerNavn: String,
    val beslutterNavn: String,
    val kontor: String,
    val html: String,
    val brevtype: PdfgenBrevtype,
    val mottaker: PdfgenMottakerType,
)

enum class PdfgenMottakerType {
    BRUKER,
    SAMHANDLER
}

enum class PdfgenBrevtype {
    VEDTAKSBREV,
    INNHENTINGSBREV,
    INFORMASJONSBREV,
    BREV

}

data class Personalia(
    val ident: Fnr,
    val fornavn: String,
    val etternavn: String,
)
