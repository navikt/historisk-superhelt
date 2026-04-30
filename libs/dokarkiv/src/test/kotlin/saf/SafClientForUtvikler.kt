package saf

import no.nav.common.consts.FellesKodeverkTema
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
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
import java.util.UUID

@Disabled
class SafClientForUtvikler {

    /**
     * Genereres i Ida  auth token med client id dev-fss.teamdokumenthandtering.saf
     *
     * https://azure-token-generator.intern.dev.nav.no/api/obo?aud=dev-fss.teamdokumenthandtering.saf
     */

    val accessToken =
        """
      """.trimIndent()

//    private val baseUrl = "https://saf.dev.intern.nav.no"
        private val baseUrl = "http://localhost:9080/saf-mock"

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

    @Test
    fun `journalposter for sak`() {
        val result = graphqlClient.dokumentoversiktFagsak(saksnummer = Saksnummer(id = 48), tema = listOf(FellesKodeverkTema.HEL))
        println(result)
        println()
        result.data?.dokumentoversiktFagsak?.journalposter?.forEach {
            println(it)
        }
    }

    @Test
    fun `journalposter for bruker`() {
        val result = graphqlClient.dokumentoversiktBruker(fnr = FolkeregisterIdent("26418823428"), tema = listOf(FellesKodeverkTema.HEL))
        println(result)
        println()
        result.data?.dokumentoversiktBruker?.journalposter?.forEach {
            println(it)
        }
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
