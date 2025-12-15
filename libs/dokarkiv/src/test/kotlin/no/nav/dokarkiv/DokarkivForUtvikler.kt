package no.nav.dokarkiv

import no.nav.common.types.Fnr
import no.nav.common.types.Saksnummer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime

@Disabled
class DokarkivForUtvikler {
    private val restTemplate: RestTemplate = getRestTemplate()

    /**
     * Logg på med AZURE AD først
     * Genereres i Ida  auth token med client id dev-fss.teamdokumenthandtering.dokarkiv
     */

    val accessToken =
        """
      """.trimIndent()

    val client = DokarkivClient(restTemplate)

    @Test
    fun `Opprett journalpost`() {
        val response =
            client.opprett(
                getMockJournalpostRequest(),
                forsokFerdigstill = true,
            )
        println(response.journalpostId) // 453990199, 453990227
        println("Journalpost ferdigstilt:" + response)
    }

    val journalPostId = EksternJournalpostId("454028810")
    val mainDokumentID = EksternDokumentInfoId("454440226")

    @Test
    fun `Oppdater journalpost`() {
        client.oppdaterJournalpost(
            journalPostId = journalPostId,
            fagsaksnummer = Saksnummer("124"),
            tittel = "Test tittel " + LocalDateTime.now(),
            bruker = Fnr("28497016101"),
            avsender = Fnr("28497016101"),
            dokumenter = listOf(DokumentMedTittel(dokumentInfoId = mainDokumentID, tittel = "Oppdatert tittel")),
        )
        println("Journalpost oppdatert:" + journalPostId)
    }

    @Test
    fun `Ferdigstill journalpost`() {
        client.ferdigstill(
            journalPostId = journalPostId,
            journalfoerendeEnhet = "4485",
        )
        println("Journalpost ferdigstilt:" + journalPostId)
    }

    @Test
    fun `Set logiske vedlegg`() {
        val dokumentInfoId = EksternDokumentInfoId("454444844")
        client.setLogiskeVedlegg(dokumentInfoId = dokumentInfoId, titler = listOf("test utviklerinfo", "dill", "dall"))
        println("Satt logiske vedlegg :" + dokumentInfoId)
    }

    private fun getRestTemplate(): RestTemplate =
        RestTemplateBuilder()
            .rootUri("https://dokarkiv-q2.dev.intern.nav.no")
            .requestFactory(JdkClientHttpRequestFactory::class.java)
            .additionalInterceptors(
                bearerTokenInterceptor(),
            ).build()

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }

}
