package no.nav.person

import no.nav.pdl.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.HttpClientErrorException
import org.springframework.http.HttpStatus

class PdlPersondataParserTest {

    private lateinit var parser: PdlPersondataParser

    @BeforeEach
    fun setUp() {
        parser = PdlPersondataParser()
    }

    @Test
    fun `parsePdlResponse returnerer komplett persondata når PDL data er gyldig`() {
        // Given
        val response = createValidPdlResponse()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertEquals("Ola Nordmann", result.navn)
        assertEquals("Ola", result.fornavn)
        assertEquals("Nordmann", result.etternavn)
        assertEquals("12345678901", result.fnr)
        assertEquals("1234567890123", result.aktorId)
        assertEquals(setOf("12345678901", "10987654321"), result.alleFnr)
        assertNull(result.doedsfall)
        assertEquals(AdressebeskyttelseGradering.UGRADERT, result.adressebeskyttelseGradering)
        assertNull(result.verge)
    }

    @Test
    fun `parsePdlResponse håndterer person med mellomnavn og komplekse data`() {
        // Given
        val response = createComplexPdlResponse()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertEquals("Kari Anne Hansen", result.navn)
        assertEquals("Kari Anne", result.fornavn)
        assertEquals("Hansen", result.etternavn)
        assertEquals(AdressebeskyttelseGradering.FORTROLIG, result.adressebeskyttelseGradering)
        assertEquals("98765432109", result.verge)
        assertEquals("2023-01-15", result.doedsfall)
    }

    @Test
    fun `parsePdlResponse håndterer alle typer adressebeskyttelse`() {
        // Given
        listOf(
            AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
            AdressebeskyttelseGradering.STRENGT_FORTROLIG,
            AdressebeskyttelseGradering.FORTROLIG,
            AdressebeskyttelseGradering.UGRADERT
        ).forEach { gradering ->
            val response = createPdlResponseWithAdressebeskyttelse(gradering)

            // When
            val result = parser.parsePdlResponse(response)

            // Then
            assertEquals(gradering, result.adressebeskyttelseGradering)
        }
    }

    @Test
    fun `parsePdlResponse håndterer person uten mellomnavn`() {
        // Given
        val response = createPdlResponseWithoutMellomnavn()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertEquals("Ola Nordmann", result.navn)
        assertEquals("Ola", result.fornavn)
        assertEquals("Nordmann", result.etternavn)
    }

    @Test
    fun `parsePdlResponse håndterer historiske identer`() {
        // Given
        val response = createPdlResponseWithHistoricalIdents()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertEquals("12345678901", result.fnr) // Aktivt FNR
        assertEquals("1234567890123", result.aktorId) // Aktiv AktørID
        assertEquals(setOf("12345678901", "98765432109", "11111111111"), result.alleFnr) // Alle FNR
    }

    @Test
    fun `parsePdlResponse kaster HttpClientErrorException ved UNAUTHORIZED feil`() {
        // Given
        val response = createPdlResponseWithError(PdlFeilkoder.UNAUTHORIZED, "Ikke tilgang")

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            parser.parsePdlResponse(response)
        }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        assertTrue(exception.message?.contains("Ikke tilgang til person i PDL") == true)
    }

    @Test
    fun `parsePdlResponse kaster HttpClientErrorException ved NOT_FOUND feil`() {
        // Given
        val response = createPdlResponseWithError(PdlFeilkoder.NOT_FOUND, "Person ikke funnet")

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            parser.parsePdlResponse(response)
        }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertTrue(exception.message?.contains("Person ikke funnet") == true)
    }

    @Test
    fun `parsePdlResponse kaster HttpClientErrorException ved SERVER_ERROR feil`() {
        // Given
        val response = createPdlResponseWithError(PdlFeilkoder.SERVER_ERROR, "Intern feil")

        // When & Then
        val exception = assertThrows<HttpClientErrorException> {
            parser.parsePdlResponse(response)
        }
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.statusCode)
        assertTrue(exception.message?.contains("Feil mot PDL") == true)
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException ved ukjent feilkode`() {
        // Given
        val response = createPdlResponseWithError("UNKNOWN_ERROR", "Ukjent feil")

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertTrue(exception.message?.contains("Fikk feilmeldinger fra PDL") == true)
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException når data er null`() {
        // Given
        val response = HentPdlResponse(data = null, errors = null)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertEquals("Fikk ingen identer fra PDL", exception.message)
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException når ingen identer returneres`() {
        // Given
        val response = HentPdlResponse(
            data = PdlData(
                hentPerson = null,
                hentIdenter = Identliste(identer = emptyList())
            ),
            errors = null
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertEquals("Fikk ingen identer fra PDL", exception.message)
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException når person er null`() {
        // Given
        val response = HentPdlResponse(
            data = PdlData(
                hentPerson = null,
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("99887766554433", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertEquals("Forventet å finne persondata", exception.message)
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException når navn mangler`() {
        // Given
        val response = HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = emptyList(),
                    doedsfall = emptyList(),
                    adressebeskyttelse = emptyList(),
                    vergemaalEllerFremtidsfullmakt = emptyList()
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertEquals("Forventet å finne navn på person", exception.message)
    }

    @Test
    fun `parsePdlResponse håndterer multiple feil og viser alle i feilmelding`() {
        // Given
        val errors = listOf(
            PdlError(
                message = "Feil 1",
                locations = emptyList(),
                path = listOf("hentPerson"),
                extensions = PdlErrorExtension(code = "ERROR_1", classification = "ClientError")
            ),
            PdlError(
                message = "Feil 2",
                locations = emptyList(),
                path = listOf("hentIdenter"),
                extensions = PdlErrorExtension(code = "ERROR_2", classification = "ClientError")
            )
        )
        val response = HentPdlResponse(data = null, errors = errors)

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            parser.parsePdlResponse(response)
        }
        assertTrue(exception.message?.contains("ERROR_1") == true)
        assertTrue(exception.message?.contains("ERROR_2") == true)
        assertTrue(exception.message?.contains("Feil 1") == true)
        assertTrue(exception.message?.contains("Feil 2") == true)
    }

    private fun createValidPdlResponse(): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Ola", null, "Nordmann")),
                    doedsfall = emptyList(),
                    adressebeskyttelse = emptyList(),
                    vergemaalEllerFremtidsfullmakt = emptyList()
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("10987654321", IdentGruppe.FOLKEREGISTERIDENT, true),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )
    }

    private fun createComplexPdlResponse(): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Kari", "Anne", "Hansen")),
                    doedsfall = listOf(Doedsfall("2023-01-15")),
                    adressebeskyttelse = listOf(Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG)),
                    vergemaalEllerFremtidsfullmakt = listOf(
                        VergemaalEllerFremtidsfullmakt(
                            vergeEllerFullmektig = VergeEllerFullmektig("98765432109")
                        )
                    )
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )
    }

    private fun createPdlResponseWithAdressebeskyttelse(gradering: AdressebeskyttelseGradering): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Test", null, "Person")),
                    doedsfall = emptyList(),
                    adressebeskyttelse = listOf(Adressebeskyttelse(gradering)),
                    vergemaalEllerFremtidsfullmakt = emptyList()
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )
    }

    private fun createPdlResponseWithoutMellomnavn(): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Ola", null, "Nordmann")),
                    doedsfall = emptyList(),
                    adressebeskyttelse = emptyList(),
                    vergemaalEllerFremtidsfullmakt = emptyList()
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
            errors = null
        )
    }

    private fun createPdlResponseWithHistoricalIdents(): HentPdlResponse {
        return HentPdlResponse(
            data = PdlData(
                hentPerson = Person(
                    navn = listOf(Navn("Historical", null, "Person")),
                    doedsfall = emptyList(),
                    adressebeskyttelse = emptyList(),
                    vergemaalEllerFremtidsfullmakt = emptyList()
                ),
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false), // Aktivt FNR
                        IdentInformasjon("98765432109", IdentGruppe.FOLKEREGISTERIDENT, true),  // Historisk FNR
                        IdentInformasjon("11111111111", IdentGruppe.FOLKEREGISTERIDENT, true),  // Historisk FNR
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false),          // Aktiv AktørID
                        IdentInformasjon("9876543210987", IdentGruppe.AKTORID, true)           // Historisk AktørID
                    )
                )
            ),
            errors = null
        )
    }

    private fun createPdlResponseWithError(errorCode: String, message: String): HentPdlResponse {
        return HentPdlResponse(
            data = null,
            errors = listOf(
                PdlError(
                    message = message,
                    locations = emptyList(),
                    path = listOf("hentPerson"),
                    extensions = PdlErrorExtension(
                        code = errorCode,
                        classification = "ClientError"
                    )
                )
            )
        )
    }
}
