package no.nav.historisk.superhelt.brev.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.BrevId
import no.nav.historisk.superhelt.brev.BrevRepository
import no.nav.historisk.superhelt.brev.BrevService
import no.nav.historisk.superhelt.brev.BrevUtkast
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.sak.SakRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak/{saksnummer}/brev")
class BrevController(
    private val brevRepository: BrevRepository,
    private val brevService: BrevService,
    private val sakRepository: SakRepository,
    private val pdfgenService: PdfgenService) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hentEllerOpprettBrev")
    @PostMapping
    fun hentEllerOpprettBrev(
        @PathVariable saksnummer: Saksnummer,
        @Valid @RequestBody request: OpprettBrevRequest): ResponseEntity<BrevUtkast> {
        val sak = sakRepository.getSak(saksnummer)

        val brev = brevService.hentEllerOpprettBrev(sak, request.type, request.mottaker)
        return ResponseEntity.ok(brev)
    }

    @Operation(operationId = "hentBrev")
    @GetMapping("{brevId}")
    fun hentBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): BrevUtkast {
        return brevRepository.getByUUid(brevId)
    }

    @Operation(operationId = "htmlBrev")
    @GetMapping("{brevId}/html", produces = ["text/html"])
    fun htmlBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): ByteArray {
        val brev = brevRepository.getByUUid(brevId)
        val sak = sakRepository.getSak(saksnummer)
        return pdfgenService.hentHtmlBrev(sak, brev)
    }

    @Operation(operationId = "oppdaterBrev")
    @PutMapping("{brevId}")
    fun oppdaterBrev(
        @PathVariable saksnummer: Saksnummer,
        @PathVariable brevId: BrevId,
        @Valid @RequestBody request: OppdaterBrevRequest): BrevUtkast {
        return brevService.oppdaterBrev(brevId, request)
    }

}
