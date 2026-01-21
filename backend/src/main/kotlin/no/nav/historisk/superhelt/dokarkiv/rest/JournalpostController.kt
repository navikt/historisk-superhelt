package no.nav.historisk.superhelt.dokarkiv.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.dokarkiv.JournalpostService
import no.nav.saf.graphql.Journalpost
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/journalpost")
@Tag(name = "Journalpost")
class JournalpostController(
    private val journalpostService: JournalpostService

) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "lastnedDokumentFraJournalpost")
    @GetMapping("/{journalpostId}/{dokumentId}", produces = ["application/pdf"])
    fun lastNedEttDokument(
        @PathVariable journalpostId: EksternJournalpostId,
        @PathVariable("dokumentId") dokumentId: EksternDokumentInfoId,
    ): ResponseEntity<ByteArray> {
        val dokument = journalpostService.lastNedEttDokument(journalpostId, dokumentId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(dokument.contentType)
            .contentLength(dokument.data.size.toLong())
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${dokument.fileName}\"")
            .body(dokument.data)
    }

    @Operation(operationId = "hentJournalpostMetaData")
    @GetMapping("/{journalpostId}/metadata")
    fun hentMetaData(
        @PathVariable journalpostId: EksternJournalpostId,
    ): Journalpost? {
//      behandlingskontekstService.ny("Hent metadata for journalpostId=$journalpostId")
//      tilgangskontrollService.validerTilgang(Ressurs.SAKSBEHANDLING, Tilgangstype.LES)
        return journalpostService.hentJournalpost(journalpostId)
    }

//   @GetMapping("/metadataMedBehandlingsnummer/{behandlingsnummer}")
//   fun hentMetaDataMedBehandlingsnummer(
//      @PathVariable behandlingsnummer: Behandlingsnummer,
//   ): Journalpost? {
//      behandlingskontekstService.ny("Hent metadata for journalpostId=$behandlingsnummer")
//      tilgangskontrollService.validerTilgang(Ressurs.SAKSBEHANDLING, Tilgangstype.LES)
//      return journalpostService.hentJournalpostMedBehandlingsnummer(behandlingsnummer)
//   }


}
