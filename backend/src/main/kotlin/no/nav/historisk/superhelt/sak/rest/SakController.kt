package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.SakService
import no.nav.historisk.superhelt.sak.Saksnummer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,
    private val sakRepository: SakRepository
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
        logger.info("Opprettet sak med saksnummer=${createdSak.saksnummer}")
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak)
    }

    @Operation(operationId = "oppdaterSak")
    @PutMapping("{saksnummer}")
    fun oppdaterSak(
        @PathVariable saksnummer: Saksnummer,
        @RequestBody @Valid req: SakUpdateRequestDto
    ): ResponseEntity<Sak> {
        val updated = sakService.updateSak(saksnummer, req)
        logger.debug("Oppdaterte sak med saksnummer=${updated.saksnummer}")
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "getSakBySaksnummer", summary = "Hent opp en sak")
    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<Sak> {
        sakRepository.getSakOrThrow(saksnummer).let {
            return ResponseEntity.ok(it)
        }
    }

}