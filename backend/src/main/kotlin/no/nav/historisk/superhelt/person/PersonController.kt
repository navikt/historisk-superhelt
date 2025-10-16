package no.nav.historisk.superhelt.person


import jakarta.validation.Valid
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/person/")
class PersonController(
    private val personService: PersonService,
    private val maskertPersonService: MaskertPersonService,
    private val tilgangsmaskinService: TilgangsmaskinService
) {

    @PostMapping()
    fun findPerson(@RequestBody @Valid request: PersonRequest): ResponseEntity<Person> {
        val persondata = personService.hentPerson(request.fnr)
        val tilgang = tilgangsmaskinService.sjekkKomplettTilgang(request.fnr)
        val maskertPersonident = maskertPersonService.maskerFnr(request.fnr)
        if (persondata == null) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(persondata.toDto(maskertPersonident, tilgang))
    }

    @GetMapping("/{maskertPersonident}")
    fun getPerson(@PathVariable maskertPersonident: MaskertPersonIdent): ResponseEntity<Person> {
        val fnr = maskertPersonService.decodeMaskertFnr(maskertPersonident)
        return findPerson(PersonRequest(fnr = fnr))
    }

}