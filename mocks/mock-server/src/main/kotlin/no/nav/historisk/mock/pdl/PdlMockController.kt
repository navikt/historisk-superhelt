package no.nav.historisk.mock.pdl

import no.nav.pdl.HentPdlResponse
import no.nav.pdl.PdlData
import org.slf4j.LoggerFactory
import org.springframework.validation.Errors
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("pdl-mock")
class PdlMockController(private val repository: PersonTestRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping()
    fun info(): Map<String, PersonTestRepository.TestPerson> {
        return repository.getAll()
    }

    @RequestMapping(value = ["/graphql"], method = [RequestMethod.OPTIONS])
    fun graphqlOptions() {
    }

    @PostMapping(value = ["/graphql"], produces = ["application/json"])
    fun graphql(@RequestBody body: GraphqlQuery<Variables>): HentPdlResponse {
        logger.debug("søker etter : {}", body)
        val ident = body.variables.ident
        val testPerson= repository.findOrCreate(ident)
        if (testPerson.avisningskode!=null){
            val errorData= testPerson.data.copy(hentPerson = null)
            val errors = pdlError(testPerson.avisningskode)
            return HentPdlResponse(data= errorData, errors=errors)
        }
        return HentPdlResponse(data= testPerson.data, errors=null)
    }


    data class Variables(
        val ident: String,
    )

}


