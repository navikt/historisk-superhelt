package no.nav.dokdist

import no.nav.common.types.EksternJournalpostId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.util.*

@Disabled
class DokdistForUtvikler {

    /**
     * Logg på med AZURE AD først
     * Genereres i Ida  auth token med client id dev-fss.teamdokumenthandtering.dokdistfordeling
     *
     */

    val accessToken =
        """
      """.trimIndent()

    val baseUrl = "http://localhost:9080/dokdist-mock"
    val client = DokdistClient(getRestClient())


    "MANGLER-ADRESSE"

    @Test
    fun `Distribuer journalpost`() {
        val request = DistribuerJournalpostRequest(
            journalpostId = EksternJournalpostId("BEST123"), //TO DO hvordan finne rett id å teste
            bestillendeFagsystem = "SUPERHELT",
            dokumentProdApp = "SUPERHELT",
            distribusjonstype = DistribuerJournalpostRequest.Distribusjonstype.VEDTAK,
            distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART,
        )
        val respons = client.distribuerJournalpost(request)
        println("sendtOk=${respons.sendtOk}, bestillingsId=${respons.bestillingsId}, feilbegrunnelse=${respons.feilbegrunnelse}")
    }

    fun `Distribuer journalpost uten adresse`() {
        val request = DistribuerJournalpostRequest(
            journalpostId = EksternJournalpostId("MANGLER-ADRESSE"),
            bestillendeFagsystem = "SUPERHELT",
            dokumentProdApp = "SUPERHELT",
            distribusjonstype = DistribuerJournalpostRequest.Distribusjonstype.VEDTAK,
            distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART,
        )
        val respons = client.distribuerJournalpost(request)
        println("sendtOk=${respons.sendtOk}, bestillingsId=${respons.bestillingsId}, feilbegrunnelse=${respons.feilbegrunnelse}")
    }
    private fun getRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
            .defaultHeaders { headers ->
                headers.set("Nav-Callid", UUID.randomUUID().toString())
            }.build()

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }


}
