package no.nav.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate
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
    private val pdlClient = PdlClient(restClient, behandlingsnummer)

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
        assertEquals(expectedData.hentPerson?.navn?.first()?.fornavn, result?.data?.hentPerson?.navn?.first()?.fornavn)
        assertEquals(expectedData.hentIdenter?.identer?.first()?.ident, result?.data?.hentIdenter?.identer?.first()?.ident)
    }

    @Test
    fun `getPersonOgIdenter returnerer PDL feil når PDL returnerer feilmeldinger`() {
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

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertNotNull(result?.errors)
        assertEquals(1, result?.errors?.size)
        assertEquals("Ikke tilgang", result?.errors?.first()?.message)
        assertEquals(PdlFeilkoder.UNAUTHORIZED, result?.errors?.first()?.extensions?.code)
    }

    @Test
    fun `getPersonOgIdenter returnerer tom data når PDL returnerer tom respons uten feil`() {
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
        assertNotNull(result)
        assertNull(result?.data)
        assertNull(result?.errors)
    }

    @Test
    fun `getPersonOgIdenter returnerer multiple feil når PDL returnerer flere feilmeldinger`() {
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

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertNotNull(result?.errors)
        assertEquals(2, result?.errors?.size)
        assertEquals("Feil 1", result?.errors?.get(0)?.message)
        assertEquals("Feil 2", result?.errors?.get(1)?.message)
    }

    @Test
    fun `getPersonOgIdenter kaster exception ved HTTP feil fra serveren`() {
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
        assertNotNull(result?.data?.hentPerson?.vergemaalEllerFremtidsfullmakt)
        assertEquals(1, result?.data?.hentPerson?.vergemaalEllerFremtidsfullmakt?.size)
        assertNotNull(result?.data?.hentPerson?.adressebeskyttelse)
        assertEquals(AdressebeskyttelseGradering.FORTROLIG,
                    result?.data?.hentPerson?.adressebeskyttelse?.first()?.gradering)
    }

    @Test
    fun `getPersonOgIdenter håndterer person uten navn`() {
        // Given
        val ident = "12345678901"
        val pdlData = PdlData(
            hentPerson = Person(
                navn = emptyList(),
                doedsfall = emptyList(),
                foedselsdato = emptyList(),
                adressebeskyttelse = emptyList(),
                vergemaalEllerFremtidsfullmakt = emptyList()
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false)
                )
            )
        )
        val response = HentPdlResponse(data = pdlData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertEquals(emptyList(), result?.data?.hentPerson?.navn)
    }

    @Test
    fun `getPersonOgIdenter håndterer person med dødsfall`() {
        // Given
        val ident = "12345678901"
        val pdlData = PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Deceased", mellomnavn = null, etternavn = "Person")),
                doedsfall = listOf(Doedsfall(doedsdato = "2023-01-15")),
                foedselsdato = emptyList(),
                adressebeskyttelse = emptyList(),
                vergemaalEllerFremtidsfullmakt = emptyList()
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false)
                )
            )
        )
        val response = HentPdlResponse(data = pdlData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertEquals(1, result?.data?.hentPerson?.doedsfall?.size)
        assertEquals("2023-01-15", result?.data?.hentPerson?.doedsfall?.first()?.doedsdato)
    }

    @Test
    fun `getPersonOgIdenter håndterer person med historiske identer`() {
        // Given
        val ident = "12345678901"
        val pdlData = PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Historical", mellomnavn = null, etternavn = "Person")),
                doedsfall = emptyList(),
                foedselsdato = emptyList(),
                adressebeskyttelse = emptyList(),
                vergemaalEllerFremtidsfullmakt = emptyList()
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false),
                    IdentInformasjon(ident = "98765432109", gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = true),
                    IdentInformasjon(ident = "1234567890123", gruppe = IdentGruppe.AKTORID, historisk = false),
                    IdentInformasjon(ident = "9876543210987", gruppe = IdentGruppe.AKTORID, historisk = true)
                )
            )
        )
        val response = HentPdlResponse(data = pdlData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        val identer = result?.data?.hentIdenter?.identer ?: emptyList()
        assertEquals(4, identer.size)
        assertEquals(2, identer.count { it.gruppe == IdentGruppe.FOLKEREGISTERIDENT })
        assertEquals(2, identer.count { it.gruppe == IdentGruppe.AKTORID })
        assertEquals(2, identer.count { it.historisk })
    }

    @Test
    fun `getPersonOgIdenter kaster exception ved ugyldig JSON respons`() {
        // Given
        val ident = "12345678901"

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess("invalid json", MediaType.APPLICATION_JSON))

        // When & Then
        assertThrows<Exception> {
            pdlClient.getPersonOgIdenter(ident)
        }
    }

    @Test
    fun `getPersonOgIdenter håndterer person med foedselsdato`() {
        // Given
        val ident = "12345678901"
        val pdlData = PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Born", mellomnavn = null, etternavn = "Person")),
                doedsfall = emptyList(),
                foedselsdato = listOf(Foedselsdato(foedselsdato = "1990-05-20")),
                adressebeskyttelse = emptyList(),
                vergemaalEllerFremtidsfullmakt = emptyList()
            ),
            hentIdenter = Identliste(
                identer = listOf(
                    IdentInformasjon(ident = ident, gruppe = IdentGruppe.FOLKEREGISTERIDENT, historisk = false)
                )
            )
        )
        val response = HentPdlResponse(data = pdlData, errors = null)

        mockServer.expect(requestTo("/graphql"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Behandlingsnummer", behandlingsnummer))
            .andRespond(withSuccess(objectMapper.writeValueAsString(response), MediaType.APPLICATION_JSON))

        // When
        val result = pdlClient.getPersonOgIdenter(ident)

        // Then
        assertNotNull(result)
        assertEquals(1, result?.data?.hentPerson?.foedselsdato?.size)
        assertEquals("1990-05-20", result?.data?.hentPerson?.foedselsdato?.first()?.foedselsdato)
    }

    private fun createValidPdlData(ident: String): PdlData {
        return PdlData(
            hentPerson = Person(
                navn = listOf(Navn(fornavn = "Ola", mellomnavn = null, etternavn = "Nordmann")),
                doedsfall = emptyList(),
                foedselsdato = emptyList(),
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
                foedselsdato = emptyList(),
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
