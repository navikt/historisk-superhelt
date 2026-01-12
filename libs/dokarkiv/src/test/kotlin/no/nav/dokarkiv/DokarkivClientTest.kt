package no.nav.dokarkiv

import no.nav.common.types.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

class DokarkivClientTest {
    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    private val restClient = RestClient.builder(restTemplate).build()
    private val dokarkivClient = DokarkivClient(restClient)

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }


    @Test
    fun `opprett skal returnere journalpost response`() {
        // Given
        val req = journalpostRequest()
        val forsokFerdigstill = true

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess(
                    // Minimal gyldig respons for JournalpostResponse
                    """
               {
                 "journalpostId":"123",
                 "journalpostferdigstilt":false,
                 "dokumenter":[]
               }
               """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                ),
            )

        // When
        val result = dokarkivClient.opprett(req, forsokFerdigstill)

        // Then
        assertThat(result.journalpostId).isEqualTo(EksternJournalpostId("123"))
        assertThat(result.journalpostferdigstilt).isFalse()
        assertThat(result.dokumenter).isEmpty()
        mockServer.verify()
    }

    @Test
    fun `opprett skal akseptere at journalpost allerede er opprettet med samme eksternref 409 response`() {
        // Given
        val req = journalpostRequest()
        val forsokFerdigstill = true

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        // Minimal gyldig respons for JournalpostResponse
                        """
               {
                 "journalpostId":"123",
                 "journalpostferdigstilt":true,
                 "dokumenter":[]
               }
               """.trimIndent(),
                    ),
            )

        // When
        val result = dokarkivClient.opprett(req, forsokFerdigstill)

        // Then
        assertThat(result.journalpostId).isEqualTo(EksternJournalpostId("123"))
        mockServer.verify()
    }

    @Test
    fun `oppdaterJournalpost skal sende put request`() {
        // Given
        val journalPostId = EksternJournalpostId("123")
        val fagsaksnummer = Saksnummer("sak123")
        val tittel = "Tittel"
        val bruker = FolkeregisterIdent("12345678901")
        val avsender = FolkeregisterIdent("09876543210")

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/journalpost/123"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(content().string(Matchers.containsString("Tittel")))
            .andExpect(content().string(Matchers.containsString("sak123")))
            .andRespond(withSuccess())

        // When
        dokarkivClient.oppdaterJournalpost(journalPostId, fagsaksnummer, tittel, bruker, avsender)

        mockServer.verify()
    }

    @Test
    fun `ferdigstill skal sende patch request`() {
        // Given
        val journalPostId = EksternJournalpostId("123")
        val journalfoerendeEnhet = "enhet"

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/journalpost/123/ferdigstill"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().string(Matchers.containsString("enhet")))
            .andRespond(withSuccess("123", MediaType.TEXT_PLAIN))

        // When
        dokarkivClient.ferdigstill(journalPostId, journalfoerendeEnhet)

        mockServer.verify()
    }

    @Test
    fun `setLogiskeVedlegg skal sende put request`() {
        // Given
        val dokumentInfoId = EksternDokumentInfoId("456")

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/dokumentInfo/456/logiskVedlegg"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(
                content().string(
                    Matchers.allOf(
                        Matchers.containsString("tittel1"),
                        Matchers.containsString("tittel2"),
                    ),
                ),
            ).andRespond(withSuccess())

        // When
        dokarkivClient.setLogiskeVedlegg(dokumentInfoId, listOf("tittel1", "tittel2"))
        mockServer.verify()
    }

    @Test
    fun `setLogiskeVedlegg skal ignorere 404 feil og logge`() {
        // Given
        val dokumentInfoId = EksternDokumentInfoId("456")

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/dokumentInfo/456/logiskVedlegg"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        // When
        dokarkivClient.setLogiskeVedlegg(dokumentInfoId, emptyList())
        // Then ingen exception
        mockServer.verify()
    }

    @Test
    fun `setLogiskeVedlegg skal kaste andre feil`() {
        // Given
        val dokumentInfoId = EksternDokumentInfoId("456")

        mockServer
            .expect(requestTo("/rest/journalpostapi/v1/dokumentInfo/456/logiskVedlegg"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        // When & Then
        assertThatThrownBy { dokarkivClient.setLogiskeVedlegg(dokumentInfoId, emptyList()) }
            .isInstanceOf(HttpClientErrorException::class.java)
        mockServer.verify()
    }

    /** Minimal testdata. Fyll inn påkrevde felter i JournalpostRequest dersom constructor endres. */
    private fun journalpostRequest(): JournalpostRequest =
        JournalpostRequest(
            tittel = "Test tittel",
            tema = EksternFellesKodeverkTema.HEL,
            journalpostType = JournalpostType.INNGAAENDE,
            avsenderMottaker =
                AvsenderMottaker(
                    id = "12345678901",
                    idType = AvsenderMottakerIdType.FNR,
                ),
            dokumenter =
                listOf(
                    Dokument(
                        tittel = "Dokument tittel",
                        brevkode = "Brevkode",
                        dokumentvarianter = listOf(),
                    ),
                ),
            bruker =
                DokarkivBruker(
                    id = "12345678901",
                    idType = BrukerIdType.FNR,
                ),
            sak =
                DokArkivSak(
                    sakstype = Sakstype.FAGSAK,
                    fagsakId = Saksnummer("123"),
                    fagsaksystem = "MOCK",
                ),
            journalfoerendeEnhet = Enhetsnummer("9999"),
            eksternReferanseId = "eksternRef",
            kanal = null,
        )

}
