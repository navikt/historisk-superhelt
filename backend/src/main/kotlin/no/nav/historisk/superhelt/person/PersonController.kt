package no.nav.historisk.superhelt.person


import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.person.tilgangsmaskin.TilgangsmaskinService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/person")
class PersonController(
    private val personService: PersonService,
    private val tilgangsmaskinService: TilgangsmaskinService
) {

    @Operation(operationId = "findPersonByFnr", summary = "Finn person basert på fødselsnummer")
    @PostMapping()
    fun findPerson(@RequestBody @Valid request: PersonRequest): ResponseEntity<Person> {
        val persondata = personService.hentPerson(request.fnr)
        val tilgang = tilgangsmaskinService.sjekkKomplettTilgang(request.fnr)
        val maskertPersonident = request.fnr.toMaskertPersonIdent()
        if (persondata == null) {
            throw IkkeFunnetException("Ingen person funnet med ident ${request.fnr}")
        }
        return ResponseEntity.ok(persondata.toDto(maskertPersonident, tilgang))
    }

    @Operation(operationId = "getPersonByMaskertIdent")
    @GetMapping("/{maskertPersonident}")
    fun getPerson(@PathVariable maskertPersonident: MaskertPersonIdent): ResponseEntity<Person> {
        val fnr = maskertPersonident.toFnr()
        return findPerson(PersonRequest(fnr = fnr))
    }

}