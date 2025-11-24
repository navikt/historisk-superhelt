package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

    @Operation(operationId = "ferdigstillSak")
    @PutMapping("status/ferdigstill")
    fun ferdigstill(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkStatusTransition(SakStatus.FERDIG)
            .checkRettighet(SakRettighet.FERDIGSTILLE)
            .checkCompleted()
            .validate()
        //TODO  håndtere retry
        sak.utbetaling?.let { utbetalingService.sendTilUtbetaling(sak) }
        sakService.ferdigstill(saksnummer)
        // sende brev

        vedtakService.fattVedtak(saksnummer)
        sakChangelog.logChange(saksnummer, "Sak $saksnummer ferdigstilt")
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
        sakService.changeStatus(saksnummer, SakStatus.TIL_ATTESTERING)

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

        sakService.changeStatus(saksnummer, SakStatus.UNDER_BEHANDLING)
        sakChangelog.logChange(saksnummer, "Sak $saksnummer er gjenåpnet")
        return ResponseEntity.ok().build()
    }

    // Henlegg
    // Avvis

}
