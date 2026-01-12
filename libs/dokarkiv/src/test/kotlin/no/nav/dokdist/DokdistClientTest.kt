package no.nav.dokdist

import no.nav.common.types.EksternJournalpostId
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

class DokdistClientTest {
    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    private val restClient = RestClient.builder(restTemplate).build()
    private val dokdistClient = DokdistClient(restClient)

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    private val request = DistribuerJournalpostRequest(
        journalpostId = EksternJournalpostId("q2123"),
        bestillendeFagsystem = "FS22",
        dokumentProdApp = "DPApp",
        distribusjonstype = DistribuerJournalpostRequest.Distribusjonstype.VEDTAK,
        distribusjonstidspunkt = DistribuerJournalpostRequest.Distribusjonstidspunkt.UMIDDELBART
    )

    @Test
    fun `distribuer skal returnere bestillingsId`() {
        mockServer
            .expect(requestTo("/rest/v1/distribuerjournalpost"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string(Matchers.containsString("q2123")))
            .andRespond(
                withSuccess(
                    """
                    {
                      "bestillingsId":"best123"
                    }
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON
                )
            )

        // When
        val result = dokdistClient.distribuerJournalpost(request)

        // Then
        assertThat(result.bestillingsId).isEqualTo("best123")
    }
    @Test
    fun `skal gir 409 om dokumentet allerede er distribuert`() {
        mockServer
            .expect(requestTo("/rest/v1/distribuerjournalpost"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                    """
                    {
                      "bestillingsId":"best123"
                    }
                    """.trimIndent()
                )
            )

        val result = dokdistClient.distribuerJournalpost(request)

        assertThat(result.bestillingsId).isEqualTo("best123")
    }

    @Test
    fun `distribuer skal kaste exception ved feil`() {
        // Given

        mockServer
            .expect(requestTo("/rest/v1/distribuerjournalpost"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        // When & Then
        assertThatThrownBy { dokdistClient.distribuerJournalpost(request) }
            .isInstanceOf(HttpClientErrorException::class.java)
    }
}
