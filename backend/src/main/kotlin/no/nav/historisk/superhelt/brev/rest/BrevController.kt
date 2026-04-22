package no.nav.historisk.superhelt.brev.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.Brev
import no.nav.historisk.superhelt.brev.BrevId
import no.nav.historisk.superhelt.brev.BrevOppdatering
import no.nav.historisk.superhelt.brev.BrevRepository
import no.nav.historisk.superhelt.brev.BrevSendingService
import no.nav.historisk.superhelt.brev.BrevService
import no.nav.historisk.superhelt.brev.BrevStatus
import no.nav.historisk.superhelt.brev.BrevType
import no.nav.historisk.superhelt.brev.finnGjeldendeBrev
import no.nav.historisk.superhelt.brev.pdfgen.PdfgenService
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.SakExtensions.auditLog
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakRettighet
import no.nav.historisk.superhelt.sak.SakValidator
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        @Valid @RequestBody request: OpprettBrevRequest): Brev {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak).checkRettighet(SakRettighet.LES).validate()

        val brevListe = brevRepository.findBySak(saksnummer)
        val brev = brevListe.finnGjeldendeBrev(request.type, request.mottaker)

        if ((brev == null || brev.status.isCompleted())) {
            val harRettighet = when (request.type) {
                BrevType.VEDTAKSBREV -> sak.rettigheter.contains(SakRettighet.SAKSBEHANDLE)
                BrevType.FRITEKSTBREV -> sak.rettigheter.contains(SakRettighet.FRITEKSTBREV)
                BrevType.HENLEGGESEBREV -> sak.rettigheter.contains(SakRettighet.SAKSBEHANDLE)
            }

            if (harRettighet) {
                return brevService.genererNyttBrev(sak, request.type, request.mottaker)
            }
        }

        return brev ?: throw IkkeFunnetException("Kunne ikke finne eller generere brev for sak")
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
            .checkAnyRettighet(SakRettighet.SAKSBEHANDLE, SakRettighet.FRITEKSTBREV)
            .validate()
        return brevRepository.oppdater(brevId, oppdatertBrev)
    }

    /** Sender annet brev enn vedtaksbrev */
    @Operation(operationId = "sendBrev")
    @PostMapping("{brevId}/send")
    fun sendAnnetBrev(@PathVariable saksnummer: Saksnummer, @PathVariable brevId: BrevId) {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkAnyRettighet(SakRettighet.SAKSBEHANDLE, SakRettighet.FRITEKSTBREV)
            .validate()
        brevSendingService.sendBrev(sak, brevId)

    }


}
