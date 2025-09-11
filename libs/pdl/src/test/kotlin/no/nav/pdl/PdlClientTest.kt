package no.nav.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PdlClientTest {

    private val behandlingsnummer = "B123"

    private val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    private val restTemplate: RestTemplate = RestTemplate()
    private var mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private  val pdlClient=PdlClient(restClient, behandlingsnummer)


    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `getPersonOgIdenter returnerer data når PDL svarer uten feil`() {
        // Given
        val ident = "12345678901"
        val expectedData = createValidPdlData(ident)
        val response = HentPdlResponse(data = expectedData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertEquals(expectedData.hentPerson?.navn?.first()?.fornavn, result.hentPerson?.navn?.first()?.fornavn)
        assertEquals(expectedData.hentIdenter?.identer?.first()?.ident, result.hentIdenter?.identer?.first()?.ident)
    }

    @Test
    fun `getPersonOgIdenter kaster FORBIDDEN når PDL returnerer UNAUTHORIZED feil`() {
        // Given
        val ident = "12345678901"
        val errors = listOf(
            PdlError(
                message = "Ikke tilgang",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = PdlFeilkoder.UNAUTHORIZED, classification = "ValidationError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            pdlClient.getPersonOgIdenter(ident)
        }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        assertEquals("403 Ikke tilgang til person i PDL: Ikke tilgang", exception.message)
    }

    @Test
    fun `getPersonOgIdenter kaster NOT_FOUND når PDL returnerer NOT_FOUND feil`() {
        // Given
        val ident = "12345678901"
        val errors = listOf(
            PdlError(
                message = "Fant ikke person",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = PdlFeilkoder.NOT_FOUND, classification = "ValidationError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            pdlClient.getPersonOgIdenter(ident)
        }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("404 Person ikke funnet: Fant ikke person", exception.message)
    }

    @Test
    fun `getPersonOgIdenter kaster INTERNAL_SERVER_ERROR når PDL returnerer SERVER_ERROR`() {
        // Given
        val ident = "12345678901"
        val errors = listOf(
            PdlError(
                message = "Intern feil",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = PdlFeilkoder.SERVER_ERROR, classification = "ExecutionError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            pdlClient.getPersonOgIdenter(ident)
        }
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.statusCode)
        assertEquals("500 Feil mot PDL: Intern feil", exception.message)
    }

    @Test
    fun `getPersonOgIdenter kaster IllegalArgumentException for ukjente feilkoder`() {
        // Given
        val ident = "12345678901"
        val errors = listOf(
            PdlError(
                message = "Ukjent feil",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = "UNKNOWN_ERROR", classification = "ValidationError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            pdlClient.getPersonOgIdenter(ident)
        }
        assertEquals("Fikk feilmeldinger fra PDL: UNKNOWN_ERROR \"[hentPerson]\" \"Ukjent feil\"", exception.message)
    }

    @Test
    fun `getPersonOgIdenter returnerer null data når PDL returnerer tom respons uten feil`() {
        // Given
        val ident = "12345678901"
        val response = HentPdlResponse(data = null, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNull(result)
    }

    @Test
    fun `getPersonOgIdenter håndterer multiple feil og viser alle i feilmelding`() {
        // Given
        val ident = "12345678901"
        val errors = listOf(
            PdlError(
                message = "Feil 1",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = "ERROR_1", classification = "ValidationError")
            ),
            PdlError(
                message = "Feil 2",
                locations = emptyList(),
                path = listOf("hentIdenter"),
                extensions = PdlErrorExtension(code = "ERROR_2", classification = "ValidationError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            pdlClient.getPersonOgIdenter(ident)
        }
        assertEquals("Fikk feilmeldinger fra PDL: ERROR_1 \"[hentPerson]\" \"Feil 1\", ERROR_2 \"[hentIdenter]\" \"Feil 2\"", exception.message)
    }

    @Test
    fun `getPersonOgIdenter håndterer HTTP feil fra serveren`() {
        // Given
        val ident = "12345678901"

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withServerError())

        // When & Then
        assertThrows<Exception> {
            pdlClient.getPersonOgIdenter(ident)
        }
    }

    @Test
    fun `getPersonOgIdenter validerer GraphQL request body inneholder riktig query og variabler`() {
        // Given
        val ident = "12345678901"
        val response = HentPdlResponse(data = createValidPdlData(ident), errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andExpect(jsonPath("$.query").exists())
            .andExpect(jsonPath("$.variables.ident").value(ident))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        pdlClient.getPersonOgIdenter(ident)

        // Then - mockServer.verify() i tearDown sjekker at forventningene ble oppfylt
    }

    @Test
    fun `getPersonOgIdenter håndterer person med vergemål og særlige adresseforhold`() {
        // Given
        val ident = "12345678901"
        val expectedData = createComplexPdlData(ident)
        val response = HentPdlResponse(data = expectedData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertNotNull(result.hentPerson?.vergemaalEllerFremtidsfullmakt)
        assertEquals(1, result.hentPerson?.vergemaalEllerFremtidsfullmakt?.size)
        assertNotNull(result.hentPerson?.adressebeskyttelse)
        assertEquals(AdressebeskyttelseGradering.FORTROLIG,
                    result.hentPerson?.adressebeskyttelse?.first()?.gradering)
    }

    private fun createValidPdlData(ident: String): PdlData {
        return PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Ola", mellomnavn = null, etternavn = "Nordmann")),
                doedsfall = emptyList(),
                adressebeskyttelse = emptyList(),
                vergemaalEllerFremtidsfullmakt = emptyList()
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false)
                )
            )
        )
    }

    private fun createComplexPdlData(ident: String): PdlData {
        return PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Kari", mellomnavn = "Anne", etternavn = "Hansen")),
                doedsfall = emptyList(),
                adressebeskyttelse = listOf(
                    Adressebeskyttelse(gradering = AdressebeskyttelseGradering.FORTROLIG)
                ),
                vergemaalEllerFremtidsfullmakt = listOf(
                    VergemaalEllerFremtidsfullmakt(
                        vergeEllerFullmektig = VergeEllerFullmektig(
                            motpartsPersonident = "98765432109"
                        )
                    )
                )
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false),
                    IdentInformasjon(ident = "2345678901234", gruppe = IdentGruppe.AKTORID, historisk = false)
                )
            )
        )
    }
}
