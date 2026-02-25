package no.nav.historisk.pdfgen

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.common.types.FolkeregisterIdent
import java.time.LocalDate


data class PdfgenRequest(
    val behandlingsnummer: String,
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
    val ident: FolkeregisterIdent,
    val fornavn: String,
    val etternavn: String,
)
