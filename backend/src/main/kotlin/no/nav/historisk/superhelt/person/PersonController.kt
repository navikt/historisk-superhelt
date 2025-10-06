package no.nav.historisk.superhelt.person


import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/person/")
class PersonController(
    private val personService: PersonService,
    private val tilgangsmaskinService: TilgangsmaskinService
) {

    @PostMapping()
    fun hentPersondataMedPersonident(@RequestBody @Valid request: PersonRequest): ResponseEntity<Person> {
        val persondata = personService.hentPerson(request.fnr)
        val tilgang = tilgangsmaskinService.sjekkKomplettTilgang(request.fnr)
        val maskertPersonident = personService.maskerFnr(request.fnr)
        if (persondata == null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(persondata.toDto(maskertPersonident, tilgang))
    }
}