package no.nav.historisk.mock.tilgangsmaskin


import no.nav.historisk.mock.pdl.PersonTestRepository
import no.nav.tilgangsmaskin.Avvisningskode
import no.nav.tilgangsmaskin.ProblemDetaljResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("tilgangsmaskin-mock")
class TilgangsmaskinMockController(private val repository: PersonTestRepository) {
    @PostMapping("/api/v1/komplett", "/api/v1/kjerne")
    fun komplett(@RequestBody personident: String): ResponseEntity<Any> {
        val person = repository.findOrCreate(personident)
        val avvisningskode = person.avvisningskode

        if (avvisningskode != null) {
            if (avvisningskode == Avvisningskode.UKJENT_PERSON) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build<Any>()
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ProblemDetaljResponse(
                    type = "mock",
                    title = avvisningskode,
                    status = HttpStatus.FORBIDDEN.value(),
                    instance = "mock",
                    brukerIdent = personident,
                    navIdent = "MOCK-SERVER",
                    begrunnelse = "Mock begrunnelse for $avvisningskode",
                    traceId = "mock",
                    kanOverstyres = false
                )
            )
        }
        return ResponseEntity.ok().build<Any>()
    }
}