package saf.rest

import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.EksternJournalpostId
import no.nav.saf.rest.SafRestClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

class SafRestClientTest {
    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private val safRestClient = SafRestClient(restClient)

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `hentDokument skal returnere dokument med korrekte data og metadata`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")
        val expectedContent = "Test dokument innhold".toByteArray()
        val expectedContentType = MediaType.APPLICATION_PDF
        val expectedFileName = "dokument.pdf"
        val expectedContentLength = expectedContent.size.toLong()

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(
                withSuccess(expectedContent, MediaType.APPLICATION_PDF)
                    .headers(
                        HttpHeaders().apply {
                            contentType = MediaType.APPLICATION_PDF
                            contentDisposition =
                                ContentDisposition
                                    .attachment()
                                    .filename(expectedFileName)
                                    .build()
                            contentLength = expectedContentLength
                        },
                    ),
            )

        // When
        val result = safRestClient.hentDokument(journalpostId, dokumentInfoId)

        // Then
        assertThat(result).isNotNull
        assertThat(result.data).isEqualTo(expectedContent)
        assertThat(result.contentType).isEqualTo(expectedContentType)
        assertThat(result.fileName).isEqualTo(expectedFileName)
        assertThat(result.contentLength).isEqualTo(expectedContentLength)
    }

    @Test
    fun `hentDokument skal håndtere manglende content disposition header`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")
        val expectedContent = "Test innhold".toByteArray()

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(
                withSuccess(expectedContent, MediaType.TEXT_PLAIN)
                    .headers(
                        HttpHeaders().apply {
                            contentType = MediaType.TEXT_PLAIN
                            contentLength = expectedContent.size.toLong()
                        },
                    ),
            )

        // When
        val result = safRestClient.hentDokument(journalpostId, dokumentInfoId)

        // Then
        assertThat(result.data).isEqualTo(expectedContent)
        assertThat(result.contentType).isEqualTo(MediaType.TEXT_PLAIN)
        assertThat(result.fileName).isNull()
        assertThat(result.contentLength).isEqualTo(expectedContent.size.toLong())
    }

    @Test
    fun `hentDokument skal bruke ARKIV som variant format`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")
        val content = "content".toByteArray()

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withSuccess(content, MediaType.APPLICATION_OCTET_STREAM))

        // When
        safRestClient.hentDokument(journalpostId, dokumentInfoId)

        // Then - verification happens in tearDown via mockServer.verify()
    }

    @Test
    fun `hentDokument skal kaste HttpClientErrorException ved 400 Bad Request`() {
        // Given
        val journalpostId = EksternJournalpostId("invalid")
        val dokumentInfoId = EksternDokumentInfoId("67890")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpClientErrorException::class.java)
            .hasMessageContaining("400")
    }

    @Test
    fun `hentDokument skal kaste HttpClientErrorException ved 401 Unauthorized`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpClientErrorException::class.java)
            .hasMessageContaining("401")
    }

    @Test
    fun `hentDokument skal kaste HttpClientErrorException ved 403 Forbidden`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpClientErrorException::class.java)
            .hasMessageContaining("403")
    }

    @Test
    fun `hentDokument skal kaste HttpClientErrorException ved 404 Not Found`() {
        // Given
        val journalpostId = EksternJournalpostId("99999")
        val dokumentInfoId = EksternDokumentInfoId("123456")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpClientErrorException::class.java)
            .hasMessageContaining("404")
    }

    @Test
    fun `hentDokument skal kaste HttpServerErrorException ved 500 Internal Server Error`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpServerErrorException::class.java)
            .hasMessageContaining("500")
    }

    @Test
    fun `hentDokument skal kaste HttpServerErrorException ved 503 Service Unavailable`() {
        // Given
        val journalpostId = EksternJournalpostId("12345")
        val dokumentInfoId = EksternDokumentInfoId("67890")

        mockServer
            .expect(requestTo("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        // When & Then
        assertThatThrownBy {
            safRestClient.hentDokument(journalpostId, dokumentInfoId)
        }.isInstanceOf(HttpServerErrorException::class.java)
            .hasMessageContaining("503")
    }
}
