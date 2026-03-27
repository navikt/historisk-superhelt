package no.nav

import no.nav.common.types.FolkeregisterIdent
import no.nav.entraproxy.InfotrygdClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.util.UUID

@Disabled
class InfotrygdClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-gcp.historisk.historisk-helt-infotrygd"
     *
     * https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-gcp.historisk.historisk-helt-infotrygd
     *
     * Dette virker ikke ende, for appen mangler noe oppsett mot ida
     */

    val accessToken =
        """
         """.trimIndent()

//    private val baseUrl = "https://historisk-helt-infotrygd.intern.dev.nav.no/"
    private val baseUrl = "http://localhost:9080/infotrygd-mock"

    val client = InfotrygdClient(getRestClient())

    @Test // IntelliJ bryr seg ikke om @Disabled derfor må denne kommenteres inn før kjøring
    fun `test hent historikk`() {
        val hentet = client.hentHistorikk(FolkeregisterIdent("11509133303"))
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
