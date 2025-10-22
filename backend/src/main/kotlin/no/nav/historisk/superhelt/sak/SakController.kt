package no.nav.historisk.superhelt.sak

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.model.Saksnummer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,

) {
    @Operation(operationId = "findSakerForPerson", summary = "Finn saker for en person")
    @GetMapping()
    fun findSaker(@RequestParam maskertPersonId: MaskertPersonIdent): ResponseEntity<List<SakDto>> {
        val fnr= maskertPersonId.toFnr()
        val saker = sakService.findSakerForPerson(fnr)
        return ResponseEntity.ok(saker)
    }

    @Operation(operationId = "createSak", summary = "opprett en ny sak")
    @PostMapping
    fun createSak(@RequestBody @Valid sak: SakCreateRequestDto): ResponseEntity<SakDto> {
        val createdSak = sakService.createSak(sak)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak.toResponseDto())
    }

    @Operation(operationId = "oppdaterSak")
    @PutMapping("{saksnummer}")
    fun oppdaterSak(@PathVariable saksnummer: Saksnummer, @RequestBody @Valid req: SakUpdateRequestDto): ResponseEntity<SakDto> {
        val updated = sakService.updateSak(saksnummer, req)
        return ResponseEntity.ok(updated)
    }

    @Operation(operationId = "getSakBySaksnummer", summary = "Hent opp en sak")
    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<SakDto> {
       sakService.getSak(saksnummer).let {
            return ResponseEntity.ok(it)
        }
    }

}