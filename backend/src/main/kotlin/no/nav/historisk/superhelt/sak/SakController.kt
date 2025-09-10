package no.nav.historisk.superhelt.sak

import jakarta.validation.Valid
import no.nav.historisk.superhelt.sak.model.Personident
import no.nav.historisk.superhelt.sak.model.SakEntity
import no.nav.historisk.superhelt.sak.model.SakRepository
import no.nav.historisk.superhelt.sak.model.Saksnummer
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak")
class SakController(private val sakService: SakService,
                    private val sakRepository: SakRepository) {

    @GetMapping
    fun getAllSaker(): ResponseEntity<List<SakResponseDto>> {
        val saker = sakRepository.findAll().map { it.toResponseDto() }
        return ResponseEntity.ok(saker)
    }
    @PostMapping
    fun createSak(@RequestBody @Valid sak: SakCreateRequestDto): ResponseEntity<SakResponseDto> {
        val createdSak = sakService.createSak(sak)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSak.toResponseDto())
    }

    @GetMapping("{saksnummer}")
    fun getSakBySaksnummer(@PathVariable saksnummer: Saksnummer): ResponseEntity<SakResponseDto> {
        val sak = sakRepository.findBySaksnummer(saksnummer)
        return if (sak != null) {
            ResponseEntity.ok(sak.toResponseDto())
        } else {
            ResponseEntity.notFound().build()
        }
    }

}