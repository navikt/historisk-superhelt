package saf.graphql


import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.dokarkiv.EksternJournalpostId
import no.nav.saf.graphql.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.json.JsonMapper
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SafGraphqlClientTest {

    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private val safClient = SafGraphqlClient(restClient)

    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .build()

    @Test
    fun `hentJournalpost skal returnere vellykket respons`() {
        val journalpostId = EksternJournalpostId("123456789")
        val jsonResponse =
            """
         {
             "data": {
                 "journalpost": {
                     "journalpostId": "$journalpostId",
                     "tittel": "Test journalpost",
                     "journalstatus": "JOURNALFOERT",
                     "sak": {
                         "fagsakId": "12345",
                         "fagsaksystem": "PP01"
                     },
                     "bruker": {
                         "id": "987654321",
                         "type": "FNR"
                     },
                     "avsenderMottaker": {
                         "id": "123456789",
                         "type": "FNR",
                         "navn": "Ola Nordmann"
                     },
                     "dokumenter": [
                         {
                             "dokumentInfoId": "doc123",
                             "tittel": "Hoveddokument",
                             "dokumentvarianter": [
                                 {
                                     "filnavn": "dokument.pdf",
                                     "filtype": "PDF",
                                     "saksbehandlerHarTilgang": true
                                 }
                             ]
                         }
                     ]
                 }
             },
             "errors": null
         }
         """.trimIndent()

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonResponse),
            )

        val result = safClient.hentJournalpost(journalpostId)

        // Verify response structure
        assertNotNull(result)
        assertNotNull(result.data)
        assertEquals(null, result.errors)

        // Verify journalpost
        val journalpost = result.data.journalpost
        assertNotNull(journalpost)
        assertEquals(journalpostId, journalpost.journalpostId)
        assertEquals("Test journalpost", journalpost.tittel)
        assertEquals(JournalStatus.JOURNALFOERT, journalpost.journalstatus)

        // Verify bruker
        val bruker = journalpost.bruker
        assertNotNull(bruker)
        assertEquals("987654321", bruker.id)
        assertEquals(BrukerIdType.FNR, bruker.type)

        // Verify avsenderMottaker
        val avsenderMottaker = journalpost.avsenderMottaker
        assertNotNull(avsenderMottaker)
        assertEquals("123456789", avsenderMottaker.id)
        assertEquals(AvsenderMottakerIdType.FNR, avsenderMottaker.type)
        assertEquals("Ola Nordmann", avsenderMottaker.navn)

        // Verify sak
        val sak = journalpost.sak
        assertNotNull(sak)
        assertEquals("12345", sak.fagsakId)
        assertEquals("PP01", sak.fagsaksystem)

        // Verify dokumenter
        val dokumenter = journalpost.dokumenter
        assertNotNull(dokumenter)
        assertEquals(1, dokumenter.size)

        val dokument = dokumenter.first()
        assertEquals(EksternDokumentInfoId("doc123"), dokument.dokumentInfoId)
        assertEquals("Hoveddokument", dokument.tittel)

        // Verify dokumentvarianter
        val dokumentvarianter = dokument.dokumentvarianter
        assertNotNull(dokumentvarianter)
        assertEquals(1, dokumentvarianter.size)

        val variant = dokumentvarianter.first()
        assertEquals("dokument.pdf", variant.filnavn)
        assertEquals("PDF", variant.filtype)
        assertEquals(true, variant.saksbehandlerHarTilgang)

        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere journalpost ikke funnet`() {
        val journalpostId = EksternJournalpostId("999999999")
        val responseWithNullData =
            HentJournalpostGraphqlResponse(
                data = HentJournalpostData(journalpost = null),
                errors =
                    listOf(
                        GraphqlError(
                            message = "Journalpost ikke funnet",
                            extensions =
                                ErrorExtensions(
                                    code = "not_found",
                                    classification = "ExecutionAborted",
                                ),
                        ),
                    ),
            )

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(responseWithNullData)),
            )

        val result = safClient.hentJournalpost(journalpostId)

        assertNotNull(result)
        assertEquals(null, result.data?.journalpost)
        assertEquals(1, result.errors?.size)
        assertEquals("Journalpost ikke funnet", result.errors?.first()?.message)
        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere ingen tilgang`() {
        val journalpostId = EksternJournalpostId("123456789")
        val accessDeniedResponse =
            HentJournalpostGraphqlResponse(
                data = null,
                errors =
                    listOf(
                        GraphqlError(
                            message = "Ikke tilgang til journalpost",
                            extensions =
                                ErrorExtensions(
                                    code = "access_denied",
                                    classification = "ExecutionAborted",
                                ),
                        ),
                    ),
            )

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(accessDeniedResponse)),
            )

        val result = safClient.hentJournalpost(journalpostId)

        assertNotNull(result)
        assertEquals(null, result.data)
        assertEquals("Ikke tilgang til journalpost", result.errors?.first()?.message)
        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere HTTP 401 Unauthorized`() {
        val journalpostId = EksternJournalpostId("123456789")

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED))

        assertThrows<HttpClientErrorException.Unauthorized> {
            safClient.hentJournalpost(journalpostId)
        }

        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere HTTP 403 Forbidden`() {
        val journalpostId = EksternJournalpostId("123456789")

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        assertThrows<HttpClientErrorException.Forbidden> {
            safClient.hentJournalpost(journalpostId)
        }

        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere HTTP 500 Internal Server Error`() {
        val journalpostId = EksternJournalpostId("123456789")

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertThrows<HttpServerErrorException.InternalServerError> {
            safClient.hentJournalpost(journalpostId)
        }

        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere ugyldig JSON respons`() {
        val journalpostId = EksternJournalpostId("123456789")

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{invalid json"),
            )

        assertThrows<Exception> {
            safClient.hentJournalpost(journalpostId)
        }

        mockServer.verify()
    }

    @Test
    fun `hentJournalpost skal håndtere GraphQL valideringsfeil`() {
        val journalpostId = EksternJournalpostId("")
        val validationErrorResponse =
            HentJournalpostGraphqlResponse(
                data = null,
                errors =
                    listOf(
                        GraphqlError(
                            message = "Validation error: journalpostId cannot be empty",
                            extensions =
                                ErrorExtensions(
                                    code = "bad_request",
                                    classification = "ExecutionAborted",
                                ),
                        ),
                    ),
            )

        mockServer
            .expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(validationErrorResponse)),
            )

        val result = safClient.hentJournalpost(journalpostId)

        assertNotNull(result)
        assertEquals(null, result.data)
        assertEquals("Validation error: journalpostId cannot be empty", result.errors?.first()?.message)
        mockServer.verify()
    }


}
