package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.infrastruktur.exception.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.exception.ValideringException
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak/{saksnummer}")
class SakActionController(
    private val sakService: SakService,
    private val sakRepository: SakRepository,
    private val sakChangelog: SakChangelog,
    private val utbetalingService: UtbetalingService,
    private val vedtakService: VedtakService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "attersterSak")
    @PutMapping("status/ferdigstill")
    fun attesterSak(@PathVariable saksnummer: Saksnummer, @RequestBody request: AttesterSakRequestDto): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        if (!request.godkjent && request.kommentar.isNullOrBlank()) {
           throw ValideringException(reason ="Valideringsfeil" , validationErrors = listOf(
               ValidationFieldError("kommentar", "Kommentar å oppgis når sak ikke godkjennes")))
        }



        if(request.godkjent){
            SakValidator(sak)
                .checkStatusTransition(SakStatus.FERDIG)
                .checkRettighet(SakRettighet.ATTESTERE)
                .checkCompleted()
                .validate()
            //TODO  håndtere retry
            sak.utbetaling?.let { utbetalingService.sendTilUtbetaling(sak) }
            sakService.ferdigstill(sak)
            // sende brev

            vedtakService.fattVedtak(saksnummer)
            sakChangelog.logChange(saksnummer, "Sak $saksnummer ferdigstilt")
        }else{
            SakValidator(sak)
                .checkStatusTransition(SakStatus.UNDER_BEHANDLING)
                .checkRettighet(SakRettighet.ATTESTERE)
                .validate()
            sakService.gjenapneSak(sak, request.kommentar!!)
            sakChangelog.logChange(saksnummer, "Sak $saksnummer returnert til saksbehandling med kommentar: ${request.kommentar}")
        }

        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "sendTilAttestering")
    @PutMapping("status/tilattestering")
    fun tilAttestering(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkStatusTransition(SakStatus.TIL_ATTESTERING)
            .checkCompleted()
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        sakService.sendTilAttestering(sak)

        sakChangelog.logChange(saksnummer, "Sak $saksnummer sendt til totrinnskontroll")
        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "gjenapneSak")
    @PutMapping("status/gjenapne")
    fun gjenapne(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        // TODO årsak mm
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkStatusTransition(SakStatus.UNDER_BEHANDLING)
            .checkRettighet(SakRettighet.GJENAPNE)
            .validate()

        sakService.gjenapneSak(sak, "Gjenåpnet via API TODO årsak")
        sakChangelog.logChange(saksnummer, "Sak $saksnummer er gjenåpnet")
        return ResponseEntity.ok().build()
    }

    // Henlegg
    // Avvis

}
