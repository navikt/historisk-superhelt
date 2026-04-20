package no.nav.infotrygd

import no.nav.common.types.FolkeregisterIdent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import tools.jackson.databind.json.JsonMapper
import java.time.LocalDate

class InfotrygdClientTest {

    private val objectMapper = JsonMapper.builder()
        .findAndAddModules()
        .build()

    private val restTemplate: RestTemplate = RestTemplate()
    private val mockServer: MockRestServiceServer = MockRestServiceServer.bindTo(restTemplate).build()

    private val restClient = RestClient.builder(restTemplate).build()
    private val infotrygdClient = InfotrygdClient(restClient)

    // ==================== hentHistorikk tests ====================

    @Test
    fun `hentHistorikk returnerer liste med historikk`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.fnr[0]").value("12345678901"))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        """
                        {
                          "personkort": [
                            {
                              "dato": "2024-01-15",
                              "fom": "2024-01-01",
                              "tom": "2024-12-31",
                              "tekst": "Ortose",
                              "kontonummer": "5122000",
                              "bevilgetBelop": "5000"
                            }
                          ]
                        }
                        """.trimIndent()
                    )
            )

        // Act
        val historikk = infotrygdClient.hentHistorikk(fnr)

        // Assert
        assertThat(historikk).hasSize(1)
        assertThat(historikk[0].dato).isEqualTo(LocalDate.of(2024, 1, 15))
        assertThat(historikk[0].fom).isEqualTo(LocalDate.of(2024, 1, 1))
        assertThat(historikk[0].tom).isEqualTo(LocalDate.of(2024, 12, 31))
        assertThat(historikk[0].tekst).isEqualTo("Ortose")
        assertThat(historikk[0].kontonummer).isEqualTo("5122000")
        assertThat(historikk[0].kontonavn).isEqualTo("Ortose")
        assertThat(historikk[0].belop).isEqualTo("5000")
        mockServer.verify()
    }

    @Test
    fun `hentHistorikk kaster exception ved ugyldig json`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{ ugyldig json }")
            )

        // Act & Assert
        assertThatThrownBy {
            infotrygdClient.hentHistorikk(fnr)
        }.isInstanceOf(RestClientException::class.java)
        mockServer.verify()
    }

    @Test
    fun `hentHistorikk returnerer tom liste`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")
        val response = InfotrygdHistorikkResponse(personkort = emptyList())

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        val historikk = infotrygdClient.hentHistorikk(fnr)

        // Assert
        assertThat(historikk).isEmpty()
        mockServer.verify()
    }

    @Test
    fun `hentHistorikk bruker betaltBelop når bevilgetBelop mangler`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")
        val response = InfotrygdHistorikkResponse(
            personkort = listOf(
                PersonkortOversiktsdetalj(
                    dato = LocalDate.of(2024, 6, 1),
                    fom = null,
                    tom = null,
                    tekst = "Parykk",
                    kontonummer = "5120000",
                    betaltBelop = "3500",
                )
            )
        )

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        val historikk = infotrygdClient.hentHistorikk(fnr)

        // Assert
        assertThat(historikk).hasSize(1)
        assertThat(historikk[0].kontonavn).isEqualTo("Parykk")
        assertThat(historikk[0].belop).isEqualTo("3500")
        mockServer.verify()
    }

    @Test
    fun `hentHistorikk setter kontonummer og kontonavn til ukjent ved manglende kontonummer`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")
        val response = InfotrygdHistorikkResponse(
            personkort = listOf(
                PersonkortOversiktsdetalj(
                    dato = LocalDate.of(2024, 3, 10),
                    fom = null,
                    tom = null,
                    tekst = "Ukjent ytelse",
                    kontonummer = null,
                )
            )
        )

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        val historikk = infotrygdClient.hentHistorikk(fnr)

        // Assert
        assertThat(historikk).hasSize(1)
        assertThat(historikk[0].kontonummer).isEqualTo("-1")
        assertThat(historikk[0].kontonavn).isEqualTo("Ukjent")
        mockServer.verify()
    }

    @Test
    fun `hentHistorikk mapper gammel kontonummerkode til riktig kontonavn`() {
        // Arrange
        val fnr = FolkeregisterIdent("12345678901")
        val response = InfotrygdHistorikkResponse(
            personkort = listOf(
                PersonkortOversiktsdetalj(
                    dato = LocalDate.of(2023, 5, 20),
                    fom = null,
                    tom = null,
                    tekst = "Protese",
                    kontonummer = "2510001",
                    bevilgetBelop = "8000",
                )
            )
        )

        mockServer.expect(requestTo("/api/hentData"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(response))
            )

        // Act
        val historikk = infotrygdClient.hentHistorikk(fnr)

        // Assert
        assertThat(historikk).hasSize(1)
        assertThat(historikk[0].kontonummer).isEqualTo("2510001")
        assertThat(historikk[0].kontonavn).isEqualTo("Protese")
        mockServer.verify()
    }
}
