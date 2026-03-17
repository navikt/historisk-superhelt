package no.nav.entraproxy

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.util.UUID

@Disabled
class EntraProxyClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-gcp.sikkerhetstjenesten.entra-proxy
     */

    val accessToken =
        """
        <lim inn ditt token her fra ida >
         """.trimIndent()

    private val baseUrl = "https://entraproxy.intern.dev.nav.no"

//    private val baseUrl = "http://localhost:9080/entra-proxy-mock"
    val client = EntraProxyClient(getRestClient())

    @Test
    fun `test hent saksbehandlers enheter`() {
        val hentet = client.hentEnheter()
        println(hentet)
    }

    @Test
    fun `test hent saksbehandlers tema`() {
        val hentet = client.hentTema()
        println(hentet)
    }
    
    private fun getRestClient(): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
            .defaultHeaders { headers ->
                headers.set("X-Correlation-ID", UUID.randomUUID().toString())
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
