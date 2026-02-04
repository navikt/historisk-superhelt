package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.BrevSendingService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakService
import no.nav.oppgave.OppgaveType
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
    private val vedtakService: VedtakService,
    private val brevSendingService: BrevSendingService,
    private val oppgaveService: OppgaveService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "attersterSak")
    @PutMapping("status/attester")
    fun attesterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody request: AttesterSakRequestDto): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.ATTESTERE)
            .validate()

        if (!request.godkjent && (request.kommentar == null || request.kommentar.trim().length <= 5)) {
            throw ValideringException(
                reason = "Valideringsfeil", validationErrors = listOf(
                    ValidationFieldError("kommentar", "Kommentar må være lengre enn 5 tegn når sak ikke godkjennes")
                )
            )
        }
        logger.info("Sak $saksnummer attestert - godkjent: ${request.godkjent}")
        if (request.godkjent) {
            attester(sak)
            ferdigstillSak(sak)
        } else {
            sendTilbakeTilSaksbehandler(sak, request)
        }

        return ResponseEntity.ok().build()
    }

    @Operation(
        operationId = "ferdigstillSak",
        description = "Rekjøring av utbetaling og brevsending for en ferdig attestert sak"
    )
    @PutMapping("status/ferdigstill")
    fun ferdigstill(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)

        if (sak.status != SakStatus.FERDIG_ATTESTERT) {
            throw ValideringException(
                reason = "Sak må være i status FERDIG_ATTESTERT for å fullføre",
                validationErrors = listOf(
                    ValidationFieldError("status", "Sak er i status ${sak.status}")
                )
            )
        }
        logger.info("Ferdigstiller sak $saksnummer på nytt")

        ferdigstillSak(sak)
        return ResponseEntity.ok().build()
    }

    private fun sendTilbakeTilSaksbehandler(
        sak: Sak,
        request: AttesterSakRequestDto) {
        SakValidator(sak)
            .checkStatusTransition(SakStatus.UNDER_BEHANDLING)
            .validate()
        sakService.endreStatus(sak, SakStatus.UNDER_BEHANDLING)
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.ATTESTERING_UNDERKJENT,
            endring = "Sak returnert til saksbehandler",
            beskrivelse = request.kommentar
        )
    }

    private fun attester(sak: Sak) {
        SakValidator(sak)
            .checkStatusTransition(SakStatus.FERDIG_ATTESTERT)
            .checkRettighet(SakRettighet.ATTESTERE)
            .checkCompleted()
            .validate()
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.ATTESTERT_SAK,
            endring = "Sak attestert ok"
        )
        sakService.endreStatus(sak, SakStatus.FERDIG_ATTESTERT)

    }

    private fun ferdigstillSak(sak: Sak) {
        //TODO  håndtere retry
        sak.vedtaksbrevBruker?.let { brevSendingService.sendBrev(sak = sak, brev = it) }
        sak.utbetaling?.let { utbetalingService.sendTilUtbetaling(sak) }

        sakService.endreStatus(sak, SakStatus.FERDIG)

        vedtakService.fattVedtak(sak.saksnummer)
        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.FERDIGSTILT_SAK,
            endring = "Sak ferdigstilt"
        )
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
        logger.info("Sak $saksnummer sent til attestering")
        sakService.endreStatus(sak, SakStatus.TIL_ATTESTERING)

        oppgaveService.ferdigstillOppgaver(
            saksnummer = saksnummer,
            type = OppgaveType.BEH_SAK
        )

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.TIL_ATTESTERING,
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

        sakService.endreStatus(sak, SakStatus.UNDER_BEHANDLING)
        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.GJENAPNET_SAK,
            endring = "Sak er gjenåpnet"
        )
        return ResponseEntity.ok().build()
    }

    // Henlegg
    // Avvis

}
