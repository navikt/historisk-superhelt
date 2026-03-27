package no.nav.ereg

import no.nav.common.types.Organisasjonsnummer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

class EregClientTest {

    private val restTemplate = RestTemplate()
    private val mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()
    private val restClient = RestClient.builder(restTemplate).build()
    private val eregClient = EregClient(restClient)

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `hentOrganisasjon returnerer organisasjon med adresse ved gyldig orgnr`() {
        val orgnr = Organisasjonsnummer("123456789")
        val responseJson = """
            {
              "organisasjonsnummer": "123456789",
              "navn": { "sammensattnavn": "FIRMA AS" },
              "postadresse": {
                "adresselinje1": "Postboks 123",
                "adresselinje2": null,
                "adresselinje3": null,
                "postnummer": "0001",
                "poststed": "OSLO",
                "landkode": "NO",
                "land": "NORGE"
              },
              "forretningsadresse": {
                "adresselinje1": "Storgata 1",
                "adresselinje2": null,
                "adresselinje3": null,
                "postnummer": "0010",
                "poststed": "OSLO",
                "landkode": "NO",
                "land": "NORGE"
              }
            }
        """.trimIndent()

        mockServer.expect(requestTo("/api/v2/organisasjon/123456789"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val result = eregClient.hentOrganisasjon(orgnr)

        assertThat(result).isNotNull
        assertThat(result!!.organisasjonsnummer).isEqualTo(orgnr)
        assertThat(result.navn).isEqualTo("FIRMA AS")
        assertThat(result.postadresse).isNotNull
        assertThat(result.postadresse!!.adresselinje1).isEqualTo("Postboks 123")
        assertThat(result.postadresse.postnummer).isEqualTo("0001")
        assertThat(result.postadresse.poststed).isEqualTo("OSLO")
        assertThat(result.postadresse.landkode).isEqualTo("NO")
        assertThat(result.forretningsadresse).isNotNull
        assertThat(result.forretningsadresse!!.adresselinje1).isEqualTo("Storgata 1")
    }

    @Test
    fun `hentOrganisasjon returnerer organisasjon uten postadresse`() {
        val orgnr = Organisasjonsnummer("987654321")
        val responseJson = """
            {
              "organisasjonsnummer": "987654321",
              "navn": { "sammensattnavn": "FORENING UTEN ADRESSE" },
              "postadresse": null,
              "forretningsadresse": null
            }
        """.trimIndent()

        mockServer.expect(requestTo("/api/v2/organisasjon/987654321"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val result = eregClient.hentOrganisasjon(orgnr)

        assertThat(result).isNotNull
        assertThat(result!!.navn).isEqualTo("FORENING UTEN ADRESSE")
        assertThat(result.postadresse).isNull()
        assertThat(result.forretningsadresse).isNull()
    }

    @Test
    fun `hentOrganisasjon returnerer null ved ukjent orgnr (404)`() {
        val orgnr = Organisasjonsnummer("000000000")

        mockServer.expect(requestTo("/api/v2/organisasjon/000000000"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withResourceNotFound())

        val result = eregClient.hentOrganisasjon(orgnr)

        assertThat(result).isNull()
    }

    @Test
    fun `hentOrganisasjon kaster exception ved serverfeil (500)`() {
        val orgnr = Organisasjonsnummer("123456789")

        mockServer.expect(requestTo("/api/v2/organisasjon/123456789"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withServerError())

        assertThrows<Exception> {
            eregClient.hentOrganisasjon(orgnr)
        }
    }

    @Test
    fun `hentOrganisasjon kaller korrekt URL-path med orgnr`() {
        val orgnr = Organisasjonsnummer("111222333")
        val responseJson = """
            {
              "organisasjonsnummer": "111222333",
              "navn": { "sammensattnavn": "TEST ORG" },
              "postadresse": {
                "adresselinje1": "Testveien 1",
                "adresselinje2": null,
                "adresselinje3": null,
                "postnummer": "1234",
                "poststed": "TESTBY",
                "landkode": "NO",
                "land": "NORGE"
              },
              "forretningsadresse": null
            }
        """.trimIndent()

        mockServer.expect(requestTo("/api/v2/organisasjon/111222333"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        eregClient.hentOrganisasjon(orgnr)

        // MockRestServiceServer.verify() i @AfterEach bekrefter at forventet request ble gjort
    }
}
