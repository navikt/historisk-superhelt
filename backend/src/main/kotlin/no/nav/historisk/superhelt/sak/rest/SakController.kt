package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.endringslogg.EndringsloggType
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,
    private val sakRepository: SakRepository,
    private val endringsloggService: EndringsloggService,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "findSakerForPerson", summary = "Finn saker for en person")
    @GetMapping()
    fun findSaker(@RequestParam maskertPersonId: MaskertPersonIdent): ResponseEntity<List<Sak>> {
        val fnr = maskertPersonId.toFnr()
        val saker = sakRepository.findSaker(fnr)
        return ResponseEntity.ok(saker)
    }

    //TODO Fjerne denne og opprette fra oppgave
    @Operation(operationId = "createSak", summary = "opprett en ny sak")
    @PostMapping
    fun createSak(@RequestBody @Valid sak: SakCreateRequestDto): ResponseEntity<Sak> {
        val createdSak = sakService.createSak(sak)
        endringsloggService.logChange(
            saksnummer = createdSak.saksnummer,
            endringsType = EndringsloggType.OPPRETTET_SAK,
            endring = "Sak opprettet"
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak)
    }

    @Operation(operationId = "oppdaterSak")
    @PutMapping("{saksnummer}")
    fun oppdaterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: SakUpdateRequestDto,
    ): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        val updated = sakService.updateSak(saksnummer, req)
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "oppdaterUtbetaling")
    @PutMapping("{saksnummer}/utbetaling")
    fun oppdaterUtbetaling(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: UtbetalingRequestDto,
    ): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.SAKSBEHANDLE)
            .validate()
        val updated = sakService.updateUtbetaling(saksnummer, req)
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "getSakBySaksnummer", summary = "Hent opp en sak")
    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<Sak> {
        val sak = sakRepository.getSak(saksnummer)
        SakValidator(sak)
            .checkRettighet(SakRettighet.LES)
            .validate()
        return ResponseEntity.ok(sak)

    }


}
