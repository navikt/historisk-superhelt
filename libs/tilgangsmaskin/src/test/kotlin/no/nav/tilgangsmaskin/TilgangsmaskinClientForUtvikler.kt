package no.nav.tilgangsmaskin

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient

@Disabled
class TilgangsmaskinClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-gcp.tilgangsmaskin.populasjonstilgangskontroll
     */

    val accessToken = """
      
      """.trimIndent()

    private val baseUrl = "https://tilgangsmaskin.intern.dev.nav.no/"
    //    private val baseUrl = "http://localhost:9080/tilgangsmaskin-mock""

    private val pdlClient = TilgangsmaskinClient(getRestClient())

    @Test
    fun `normal`() {
        val personInfo = pdlClient.komplett("28498914510")
        println(personInfo)
    }

    @Test
    fun `egen ansatt`() {
        val personInfo = pdlClient.komplett("10507646250")
        println(personInfo)
    }

    @Test
    fun `død`() {
        val personInfo = pdlClient.komplett("04457215563")
        println(personInfo)
    }

    @Test
    fun `fortrolig adresse`() {
        val personInfo = pdlClient.kjerne("19475832941")
        println(personInfo)
    }

    @Test
    fun `Ikke funnet`() {
        val personInfo = pdlClient.kjerne("tullball")
        println(personInfo)
    }

    private fun getRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
//            .defaultHeaders { headers ->
//                headers.set("Nav-Call-Id", UUID.randomUUID().toString())
//            }
            .build()
    }

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }
    }


}