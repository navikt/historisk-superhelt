package no.nav.dokarkiv

import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import java.util.*

@Disabled
class DokarkivForUtvikler {

    /**
     * Logg på med AZURE AD først
     * Genereres i Ida  auth token med client id dev-fss.teamdokumenthandtering.dokarkiv
     */

    val accessToken =
        """
      """.trimIndent()

//    val baseUrl = "https://dokarkiv-q2.dev.intern.nav.no"
    val baseUrl = "http://localhost:9080/dokarkiv-mock"
    val client = DokarkivClient(getRestClient())

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
            bruker = FolkeregisterIdent("28497016101"),
            avsender = FolkeregisterIdent("28497016101"),
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
