package no.nav.historisk.superhelt.sak

import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.model.Saksnummer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak")
class SakController(
    private val sakService: SakService,

) {

    @GetMapping()
    fun findSaker(@RequestParam maskertPersonId: MaskertPersonIdent): ResponseEntity<List<SakDto>> {
        val fnr= maskertPersonId.toFnr()
        val saker = sakService.findSakForPerson(fnr)
        return ResponseEntity.ok(saker)
    }
    @PostMapping
    fun createSak(@RequestBody @Valid sak: SakCreateRequestDto): ResponseEntity<SakDto> {
        val createdSak = sakService.createSak(sak)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak.toResponseDto())
    }

    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<SakDto> {
       sakService.findBySaksnummer(saksnummer)?.let {
            return ResponseEntity.ok(it)
        }
        return ResponseEntity.notFound().build()

    }

}