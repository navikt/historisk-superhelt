package no.nav.historisk.superhelt.brev.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.*
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
    private val pdfgenService: PdfgenService,
    private val brevSendingService: BrevSendingService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hentEllerOpprettBrev")
    @PostMapping
    fun hentEllerOpprettBrev(
        @PathVariable saksnummer: Saksnummer,
        @Valid @RequestBody request: OpprettBrevRequest): ResponseEntity<Brev> {
        val sak = sakRepository.getSak(saksnummer)

        val brev = brevService.hentEllerOpprettBrev(sak, request.type, request.mottaker)
        return ResponseEntity.ok(brev)
    }

    @Operation(operationId = "hentBrev")
    @GetMapping("{brevId}")
    fun hentBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): Brev {
        return brevRepository.getByUUid(brevId)
    }

    @Operation(operationId = "htmlBrev")
    @GetMapping("{brevId}/html", produces = ["text/html"])
    fun htmlBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): ByteArray {
        val brev = brevRepository.getByUUid(brevId)
        val sak = sakRepository.getSak(saksnummer)
        return pdfgenService.genererHtml(sak, brev)
    }

    @Operation(operationId = "oppdaterBrev")
    @PutMapping("{brevId}")
    fun oppdaterBrev(
        @PathVariable saksnummer: Saksnummer,
        @PathVariable brevId: BrevId,
        @Valid @RequestBody request: OppdaterBrevRequest): Brev {
        val oppdatertBrev = BrevOppdatering(
            tittel = request.tittel,
            innhold = request.innhold,
            status = BrevStatus.UNDER_ARBEID
        )
        // TODO sjekke tilgang i sak
        return brevRepository.oppdater(brevId, oppdatertBrev)
    }

    @Operation(operationId = "sendBrev")
    @PostMapping("{brevId}/send")
    fun sendAnnetBrev(
        brevController: BrevController, @PathVariable saksnummer: Saksnummer,
        @PathVariable brevId: BrevId,
    ) {
        val brev = brevController.brevRepository.getByUUid(brevId)
        val sak = brevController.sakRepository.getSak(saksnummer)

        brevSendingService.sendBrev(sak, brev)

    }


}
