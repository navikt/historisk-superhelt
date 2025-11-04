package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.*
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,
    private val sakRepository: SakRepository,
    private val sakChangelog: SakChangelog,
    private val utbetalingService: UtbetalingService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "findSakerForPerson", summary = "Finn saker for en person")
    @GetMapping()
    fun findSaker(@RequestParam maskertPersonId: MaskertPersonIdent): ResponseEntity<List<Sak>> {
        val fnr = maskertPersonId.toFnr()
        val saker = sakRepository.findSaker(fnr)
        return ResponseEntity.ok(saker)
    }

    @Operation(operationId = "createSak", summary = "opprett en ny sak")
    @PostMapping
    fun createSak(@RequestBody @Valid sak: SakCreateRequestDto): ResponseEntity<Sak> {
        val createdSak = sakService.createSak(sak)
        sakChangelog.logChange(createdSak.saksnummer, "Sak opprettet")
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak)
    }

    @Operation(operationId = "oppdaterSak")
    @PutMapping("{saksnummer}")
    fun oppdaterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: SakUpdateRequestDto
    ): ResponseEntity<Sak> {
        val updated = sakService.updateSak(saksnummer, req)
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "getSakBySaksnummer", summary = "Hent opp en sak")
    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<Sak> {
        sakRepository.getSakOrThrow(saksnummer).let {
            return ResponseEntity.ok(it)
        }
    }


    @Operation(operationId = "ferdigstillSak")
    @PutMapping("{saksnummer}/status/ferdigstill")
    fun ferdigstill(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        val sak = sakRepository.getSakOrThrow(saksnummer)
        SakValidator(sak)
//            .validateStatusTransition(SakStatus.FERDIG)
            .validateCompleted()
//            .validateSaksbehandlerErIkkeAttestant()


        //TODO lage jobb for sende brev, utbetale, lukke saker..
        sakService.changeStatus(saksnummer, SakStatus.FERDIG)
        sakChangelog.logChange(saksnummer, "Sak $saksnummer ferdigstilt")
        sak.utbetaling?.let { utbetalingService.sendTilUtbetaling(sak) }
        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "sendTilAttestering")
    @PutMapping("{saksnummer}/status/tilattestering")
    fun tilAttestering(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        // ikke endre om status allerede er er tilattestering
        val sak = sakRepository.getSakOrThrow(saksnummer)
        SakValidator(sak)
            .validateStatusTransition(SakStatus.TIL_ATTESTERING)
            .validateCompleted()
        sakService.changeStatus(saksnummer, SakStatus.TIL_ATTESTERING)
        // håndtere saker mm
        sakChangelog.logChange(saksnummer, "Sak $saksnummer sendt til totrinnskontroll")
        return ResponseEntity.ok().build()
    }

    @Operation(operationId = "gjenapneSak")
    @PutMapping("{saksnummer}/status/gjenapne")
    fun gjenapne(@PathVariable saksnummer: Saksnummer): ResponseEntity<Unit> {
        //TODO årsak
        val sak = sakRepository.getSakOrThrow(saksnummer)
        SakValidator(sak)
            .validateStatusTransition(SakStatus.UNDER_BEHANDLING)

        // Håntere saker mm
        sakService.changeStatus(saksnummer, SakStatus.UNDER_BEHANDLING)
        sakChangelog.logChange(saksnummer, "Sak $saksnummer er gjenåpnet")
        return ResponseEntity.ok().build()
    }


    // Gjenåpne sak
    // Henlegg
    // Avvis


}