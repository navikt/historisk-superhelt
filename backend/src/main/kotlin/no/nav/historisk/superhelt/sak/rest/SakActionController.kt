package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
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
    private val endringsloggService: EndringsloggService,
    private val utbetalingService: UtbetalingService,
    private val vedtakService: VedtakService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "attersterSak")
    @PutMapping("status/ferdigstill")
    fun attesterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody request: AttesterSakRequestDto): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        if (!request.godkjent && (request.kommentar == null || request.kommentar.trim().length <= 5)) {
            throw ValideringException(
                reason = "Valideringsfeil", validationErrors = listOf(
                    ValidationFieldError("kommentar", "Kommentar må være lengre enn 5 tegn når sak ikke godkjennes")
                )
            )
        }

        if (request.godkjent) {
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
            endringsloggService.logChange(saksnummer = saksnummer,
                endingsType = EndringsloggType.ATTESTERT_SAK,
                endring = "Sak ferdigstilt")
        } else {
            SakValidator(sak)
                .checkStatusTransition(SakStatus.UNDER_BEHANDLING)
                .checkRettighet(SakRettighet.ATTESTERE)
                .validate()
            sakService.gjenapneSak(sak, request.kommentar!!)
            endringsloggService.logChange(
                saksnummer = saksnummer,
                endingsType = EndringsloggType.ATTESTERING_UNDERKJENT,
                endring = "Sak returnert til saksbehandler",
                beskrivelse = request.kommentar
            )
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

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endingsType = EndringsloggType.TIL_ATTESTERING,
            endring = "Sak sendt til totrinnskontroll"
        )
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
        endringsloggService.logChange(saksnummer = saksnummer,
            endingsType = EndringsloggType.GJENAPNET_SAK,
            endring = "Sak er gjenåpnet")
        return ResponseEntity.ok().build()
    }

    // Henlegg
    // Avvis

}
