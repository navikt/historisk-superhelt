package no.nav.historisk.mock.dokarkiv

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("dokarkiv-mock")
class DokarkivController {

    @PostMapping("/rest/journalpostapi/v1/journalpost")
    fun opprettJournalpostMock(@RequestParam("forsoekFerdigstill") forsokFerdigStill: Boolean = false) : JournalpostResponse {
        return JournalpostResponse(journalpostId = "112233", journalpostferdigstilt = forsokFerdigStill, dokumenter = emptyList(), melding="OK fra dokarkiv-mock")
    }
}

data class JournalpostRequest(
    val avsenderMottaker: AvsenderMottaker? = null,
    val behandlingstema: String? = null,
    var bruker: Bruker? = null,
    val dokumenter: List<Dokument>,
    val eksternReferanseId: String? = null,
    val journalfoerendeEnhet: String? = null,
    val journalpostType: String? = null,
    val kanal: String? = null,
    val sak: Sak? = null,
    val tema: String? = null,
    val tittel: String? = null,
)

data class AvsenderMottaker(
    val id: String? = null,
    val idType: String? = null,
    val land: String? = null,
    val navn: String,
)

data class Bruker(
    val id: String,
    val idType: String,
)

data class Dokument(
    val brevkode: String? = null,
    val dokumentKategori: String? = null,
    val dokumentvarianter: List<Dokumentvarianter>,
    val tittel: String,
)

data class Dokumentvarianter(
    val filnavn: String,
    val filtype: String,
    val fysiskDokument: ByteArray,
    val variantformat: String,
)

data class Sak(
    val sakstype: String? = null,
)


data class JournalpostResponse(
    val dokumenter: List<DokumentInfo>,
    val journalpostId: String,
    val journalpostferdigstilt: Boolean,
    val journalstatus: String? = null,
    val melding: String? = null,
)

data class DokumentInfo(
    val brevkode: String? = null,
    val dokumentInfoId: String? = null,
    val tittel: String? = null,
)
