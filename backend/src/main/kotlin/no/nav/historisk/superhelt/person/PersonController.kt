package no.nav.historisk.superhelt.person


import jakarta.validation.Valid
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/person/")
class PersonController(
    private val personService: PersonService,
) {
//    @PreAuthorize("@tilgangsmaskin.harTilgang(#request.fnr)")
    @PostMapping()
    fun hentPersondataMedPersonident(@RequestBody @Valid request: PersonRequest): Person {
       // TODO legge inn avvisningsårsak i responsen
        val persondata = personService.hentPerson(request.fnr)
        val maskertPersonident = personService.maskerFnr(request.fnr)
        return persondata.toDto(maskertPersonident)
    }
}