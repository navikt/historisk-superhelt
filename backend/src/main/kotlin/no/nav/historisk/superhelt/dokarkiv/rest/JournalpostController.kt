package no.nav.historisk.superhelt.dokarkiv.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.dokarkiv.JournalpostService
import no.nav.historisk.superhelt.infrastruktur.audit.AuditLog
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
        val journalpost = journalpostService.hentJournalpost(journalpostId)
        if (journalpost != null) {
            val fnr = journalpost.bruker?.id?.let { FolkeregisterIdent(it) }
            if (fnr != null) {
                AuditLog.log(
                    fnr = fnr,
                    message = "Hentet metadata for journalpost fra arkivet",
                    customIdentifierAndValue = Pair("journalpostId", journalpostId.value)
                )
            }
        }

        return journalpost
    }

    @Operation(operationId = "finnJournalposterForSak")
    @GetMapping("/sak/{saksnummer}")
    fun finnJournalposterForSak(
        @PathVariable saksnummer: Saksnummer,
    ): List<Journalpost> {
        return journalpostService.finnJournalposter(saksnummer)
    }


}
