package no.nav.historisk.superhelt.brev.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.*
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.sak.SakExtensions.auditLog
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakRettighet
import no.nav.historisk.superhelt.sak.SakValidator
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
        SakValidator(sak)
            .checkRettighet(SakRettighet.LES)
            .validate()
        val brev = brevService.hentEllerOpprettBrev(sak, request.type, request.mottaker)
        sak.auditLog("Henter brev ${brev.uuid} for sak")
        return ResponseEntity.ok(brev)
    }

    @Operation(operationId = "hentBrev")
    @GetMapping("{brevId}")
    fun hentBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): Brev {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.LES)
            .validate()
        val brev = brevRepository.getByUUid(brevId)
        sak.auditLog("Henter brev ${brev.uuid} for sak")
        return brev
    }

    @Operation(operationId = "htmlBrev")
    @GetMapping("{brevId}/html", produces = ["text/html"])
    fun htmlBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId): ByteArray {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.LES)
            .validate()

        val brev = brevRepository.getByUUid(brevId)
        val html = pdfgenService.genererHtml(sak, brev)
        sak.auditLog("Henter htmlbrev ${brev.uuid} for sak")
        return html
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
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        return brevRepository.oppdater(brevId, oppdatertBrev)
    }

    @Operation(operationId = "sendBrev")
    @PostMapping("{brevId}/send")
    fun sendAnnetBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId) {
        val brev = brevRepository.getByUUid(brevId)
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        brevSendingService.sendBrev(sak, brev)

    }


}
