package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.brev.BrevSendingService
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.validation.ValidationFieldError
import no.nav.historisk.superhelt.infrastruktur.validation.ValideringException
import no.nav.historisk.superhelt.oppgave.OppgaveService
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.vedtak.VedtakService
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
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
            ferdigstill(sak.saksnummer)
        } else {
            sendTilbakeTilSaksbehandler(sak, request)
        }
        oppgaveService.ferdigstillOppgaver(saksnummer, OppgaveType.GOD_VED)
        return ResponseEntity.ok().build()
    }

    @Operation(
        operationId = "ferdigstillSak",
        description = "Rekjøring av utbetaling og brevsending for en ferdig attestert sak"
    )
    @PutMapping("status/ferdigstill")
    fun ferdigstill(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkStatusTransition(SakStatus.FERDIG)
            .validate()

        logger.info("Ferdigstiller sak $saksnummer på nytt")

        sak.vedtaksbrevBruker?.let { brevSendingService.sendBrev(sak = sak, brev = it) }
        sak.utbetaling?.let { utbetalingService.sendTilUtbetaling(sak) }

        sakService.endreStatus(sak, SakStatus.FERDIG)
        vedtakService.fattVedtak(sak.saksnummer)

        endringsloggService.logChange(
            saksnummer = sak.saksnummer,
            endringsType = EndringsloggType.FERDIGSTILT_SAK,
            endring = "Sak ferdigstilt"
        )
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

        oppgaveService.opprettOppgave(
            type = OppgaveType.BEH_UND_VED,
            sak = sak,
            beskrivelse = "Sak ${sak.saksnummer} underkjent i attestering med kommentar: ${request.kommentar}",
            tilordneTil = sak.saksbehandler.navIdent
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

        oppgaveService.opprettOppgave(
            type = OppgaveType.GOD_VED,
            sak = sak,
            beskrivelse = "Attestering av sak ${sak.type.navn} med saksnummer ${sak.saksnummer} saksbehandlet av ${sak.saksbehandler.navIdent}",
            tilordneTil = null
        )
        oppgaveService.ferdigstillOppgaver(saksnummer, OppgaveType.BEH_SAK, OppgaveType.BEH_UND_VED)

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.TIL_ATTESTERING,
            endring = "Sak sendt til totrinnskontroll"
        )
        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "feilregisterSak")
    @PutMapping("status/feilregister")
    fun feilregister(
        @PathVariable saksnummer: Saksnummer,
        @Valid @RequestBody request: FeilregisterRequestDto): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.FEILREGISTERE)
            .validate()
        logger.info("Sak $saksnummer er feilregistert")
        sakService.endreStatus(sak, SakStatus.FEILREGISTRERT)


        oppgaveService.ferdigstillOppgaver(saksnummer, OppgaveType.BEH_SAK)

        oppgaveService.opprettOppgave(
            type = OppgaveType.BEH_SAK_MK,
            sak = sak,
            beskrivelse = """Sak ${sak.saksnummer} er feilregistrert med årsak: ${request.aarsak} 
                
                 Det må ryddes opp i journalposter knyttet til denne saken
            """.trimIndent(),
            tilordneTil = getAuthenticatedUser().navIdent,
            // Setter applikasjon til null så denne behandles i helhet i gosys
            behandlesAvApplikasjon = null
        )

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.FEILREGISTERT,
            endring = "Sak feilregistrert",
            beskrivelse = "Årsak: ${request.aarsak}"
        )
        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "henleggSak")
    @PutMapping("status/henlegg")
    fun henleggSak(
        @PathVariable saksnummer: Saksnummer,
        @Valid @RequestBody request: HenlagtSakRequestDto): ResponseEntity<Unit> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.HENLEGGE)
            .validate()

        logger.info("Henlegger sak $saksnummer")
        sakRepository.updateSak(sak.saksnummer, UpdateSakDto(
            status = SakStatus.FERDIG,
            saksbehandler = getAuthenticatedUser().navUser,
            vedtaksResultat = VedtaksResultat.HENLAGT
        ))
        brevSendingService.sendBrev(sak, request.hendleggelseBrevId)

        oppgaveService.ferdigstillOppgaver(saksnummer)

        endringsloggService.logChange(
            saksnummer = saksnummer,
            endringsType = EndringsloggType.HENLEGG_SAK,
            endring = "Sak henlagt",
            beskrivelse = "Årsak: ${request.aarsak}"
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

}
