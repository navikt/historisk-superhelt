package no.nav.historisk.superhelt.person


import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/person/")
class PersonController(
    private val personService: PersonService,
    private val tilgangsmaskinService: TilgangsmaskinService
) {

    @PostMapping()
    fun findPerson(@RequestBody @Valid request: PersonRequest): ResponseEntity<Person> {
        val persondata = personService.hentPerson(request.fnr)
        val tilgang = tilgangsmaskinService.sjekkKomplettTilgang(request.fnr)
        val maskertPersonident = request.fnr.toMaskertPersonIdent()
        if (persondata == null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(persondata.toDto(maskertPersonident, tilgang))
    }

    @GetMapping("/{maskertPersonident}")
    fun getPerson(@Size(max = 100) @PathVariable maskertPersonident: MaskertPersonIdent): ResponseEntity<Person> {
        val fnr = maskertPersonident.toFnr()?: return ResponseEntity.notFound().build()
        return findPerson(PersonRequest(fnr = fnr))
    }

}