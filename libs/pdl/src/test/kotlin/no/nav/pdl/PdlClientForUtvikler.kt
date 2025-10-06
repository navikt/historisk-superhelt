package no.nav.pdl

import no.nav.person.PdlPersondataParser
import no.nav.person.Persondata
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.util.UUID

@Disabled
class PdlClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-fss.pdl.pdl-api
     */

    val accessToken = """
        
      """.trimIndent()

    private val baseUrl = "https://pdl-api.dev.intern.nav.no"
    //    private val baseUrl = "http://localhost:9080/pdl-mock"

    private val pdlClient = PdlClient(getRestClient(), "B986")

    private fun getAndParse(ident: String): Persondata? {
        val response=pdlClient.getPersonOgIdenter(ident)
        println(response)
        return PdlPersondataParser().parsePdlResponse(response)
    }

    @Test
    fun `egen ansatt`() {
        val personInfo = getAndParse("10507646250")
        println(personInfo)
    }


    @Test
    fun `død`() {
        val personInfo = getAndParse("04457215563")
        println(personInfo)
    }

    @Test
    fun `fortrolig adresse`() {
        val personInfo = getAndParse("19475832941")
        println(personInfo)
    }

    @Test
    fun `Ikke funnet`() {
        val personInfo = getAndParse("12345678901")
        println(personInfo)
    }

    private fun getRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
            .defaultHeaders { headers ->
                headers.set("Nav-Call-Id", UUID.randomUUID().toString())
            }
            .build()
    }

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }
    }


}