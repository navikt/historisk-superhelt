package saf

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.saf.graphql.SafGraphqlClient
import no.nav.saf.rest.SafRestClient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestClient
import java.io.File
import java.util.*

@Disabled
class SafClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-fss.teamdokumenthandtering.saf
     */

    val accessToken =
        """
      
      """.trimIndent()

    private val baseUrl = "https://saf.dev.intern.nav.no"
    //    private val baseUrl = "http://localhost:9080/pdl-mock"

    private val graphqlClient = SafGraphqlClient(getRestClient())
    private val restClient = SafRestClient(getRestClient())

    @Test
    fun `hent journalpost ukjent`() {
        val journalPost = graphqlClient.hentJournalpost(EksternJournalpostId("ukjent"))
        println(journalPost)
    }

    @Test
    fun `hent journalpost funnet`() {
        val journalPost = graphqlClient.hentJournalpost(EksternJournalpostId("453863071"))
        println(journalPost)
    }

    @Test
    fun `last ned dokument`() {
        val doc =
            restClient.hentDokument(
                journalpostId = EksternJournalpostId("453863071"),
                dokumentInfoId =
                    EksternDokumentInfoId(
                        "454256793",
                    ),
            )

        // Save document to file
        val fileName = "target/${doc.fileName}"
        val file = File(fileName)
        file.writeBytes(doc.data)
        println("Dokument saved to: ${file.absolutePath}")
        println(doc)
    }

    private fun getRestClient(): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(bearerTokenInterceptor())
            .defaultHeaders { headers ->
                headers.set("Nav-Call-Id", UUID.randomUUID().toString())
            }.build()

    private fun bearerTokenInterceptor(): ClientHttpRequestInterceptor =
        ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            request.headers.setBearerAuth(accessToken)
            execution.execute(request, body!!)
        }


}
