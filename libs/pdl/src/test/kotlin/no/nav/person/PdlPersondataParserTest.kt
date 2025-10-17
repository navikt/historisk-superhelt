package no.nav.person

import no.nav.pdl.*
import no.nav.pdl.Doedsfall
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

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

        assertEquals("Ola Nordmann", result?.navn)
        assertEquals("Ola", result?.fornavn)
        assertEquals("Nordmann", result?.etternavn)
        assertThat(result?.fnr).isEqualTo(Fnr("12345678901"))
        assertEquals("1234567890123", result?.aktorId)
        assertThat(result?.alleFnr?.map { it.value }).contains("12345678901", "10987654321")
        assertNull(result?.doedsfall)
        assertNull(result?.adressebeskyttelseGradering)
        assertNull(result?.verge)
        assertThat(result?.verge).isNull()
    }

    @Test
    fun `parsePdlResponse håndterer person med mellomnavn og komplekse data`() {
        // Given
        val response = createComplexPdlResponse()

        // When
        val result = parser.parsePdlResponse(response)
        assertEquals("Kari Anne Hansen", result?.navn)
        assertEquals("Kari Anne", result?.fornavn)
        assertEquals("Hansen", result?.etternavn)
        assertEquals(AdressebeskyttelseGradering.FORTROLIG, result?.adressebeskyttelseGradering)
        assertEquals("2023-01-15", result?.doedsfall)
        assertThat(result?.verge).isEqualTo(Fnr("98765432109"))
        assertThat(result?.doedsfall).isEqualTo("2023-01-15")
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

            assertEquals(gradering, result?.adressebeskyttelseGradering)
            assertThat(result?.adressebeskyttelseGradering).isEqualTo(gradering)
        }
    }

    @Test
    fun `parsePdlResponse håndterer person uten mellomnavn`() {
        // Given
        val response = createPdlResponseWithoutMellomnavn()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertEquals("Ola Nordmann", result?.navn)
        assertEquals("Ola", result?.fornavn)
        assertEquals("Nordmann", result?.etternavn)
    }

    @Test
    fun `parsePdlResponse håndterer historiske identer`() {
        // Given
        val response = createPdlResponseWithHistoricalIdents()

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertThat(result?.fnr).isEqualTo(Fnr("12345678901")) // Aktivt FNR
        assertEquals("1234567890123", result?.aktorId) // Aktiv AktørID
        assertThat(result?.alleFnr?.map { it.value }).contains("12345678901", "98765432109", "11111111111") // Alle FNR
    }

    @Test
    fun `parsePdlResponse returnerer person uten tilgang ved feilkode unauthorized`() {
        // Given
        val response = createPdlResponseWithError(PdlFeilkoder.UNAUTHORIZED, "Ikke tilgang")

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertNotNull(result)
        assertThat(result?.navn).contains("*")
        assertThat(result?.harTilgang).isFalse()
    }

    @Test
    fun `parsePdlResponse kaster HttpClientErrorException ved NOT_FOUND feil`() {
        // Given
        val response = createPdlResponseWithError(PdlFeilkoder.NOT_FOUND, "Person ikke funnet")

        // When
        val result = parser.parsePdlResponse(response)

        // Then
        assertNull(result)
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
    fun `parsePdlResponse kaster RuntimeExeception ved ukjent feilkode`() {
        // Given
        val response = createPdlResponseWithError("UNKNOWN_ERROR", "Ukjent feil")

        // When & Then
        assertThatThrownBy { parser.parsePdlResponse(response) }
            .isInstanceOf(RuntimeException::class.java)
            .hasMessageContaining("Uventet feil fra PDL")
    }

    @Test
    fun `parsePdlResponse kaster IllegalArgumentException når alle data er null`() {
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
    fun `parsePdlResponse gir default navn og ingen tilgang når person er null`() {
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

        // When
        val result = parser.parsePdlResponse(response)
        assertNotNull(result)
        assertEquals(false, result?.harTilgang)
        assertThat(result).isNotNull
        assertThat(result?.harTilgang).isFalse()


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

        val exception = assertThrows<RuntimeException> {
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
            data = PdlData(
                hentPerson = null,
                hentIdenter = Identliste(
                    identer = listOf(
                        IdentInformasjon("12345678901", IdentGruppe.FOLKEREGISTERIDENT, false),
                        IdentInformasjon("1234567890123", IdentGruppe.AKTORID, false)
                    )
                )
            ),
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
