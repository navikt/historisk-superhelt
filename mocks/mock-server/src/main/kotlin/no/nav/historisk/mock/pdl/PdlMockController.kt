package no.nav.historisk.mock.pdl

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("pdl-mock")
class PdlMockController() {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val repository = mutableMapOf<String, String>()

    @GetMapping()
    fun info(): Map<String, String> {
        return repository
    }

    @RequestMapping(value = ["/graphql"], method = [RequestMethod.OPTIONS])
    fun graphqlOptions() {
    }

    @PostMapping(value = ["/graphql"], produces = ["application/json"])
    fun graphql(@RequestBody body: GraphqlQuery<Variables>): String {
        logger.debug("søker etter : {}", body)
        val ident = body.variables.ident
        return repository[ident]
            ?: generateAndCacheResponse(ident)
    }

    private fun generateAndCacheResponse(ident: String): String {
        val response= generatePdlTestdata(ident)
        repository[ident] = response
        logger.info("Registerer ny person i PDL-mock: $ident -> $response")
        return pdlResponse
    }

    data class Variables(
        val ident: String,
    )

}


