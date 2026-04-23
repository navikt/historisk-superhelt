package no.nav.entraproxy

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestTemplate

class EntraProxyClientTest {

    private val restTemplate = RestTemplate()
    private val mockServer = MockRestServiceServer.bindTo(restTemplate).build()
    private val restClient = RestClient.builder(restTemplate).build()
    private val client = EntraProxyClient(restClient)

    @AfterEach
    fun tearDown() {
        mockServer.verify()
    }

    @Test
    fun `hentEnheter returnerer liste med enheter`() {
        val json = """
            [
              {"enhetnummer": "4488", "navn": "NAV Vest-Viken"},
              {"enhetnummer": "0300", "navn": "NAV Oslo"}
            ]
        """.trimIndent()

        mockServer.expect(requestTo("/api/v1/enhet"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
            )

        val result = client.hentEnheter()

        assertThat(result).hasSize(2)
        assertThat(result).extracting<String> { it.navn  }.contains("NAV Vest-Viken", "NAV Oslo")
        assertThat(result).extracting<String> { it.enhetnummer.value  }.contains("4488", "0300")
    }

    @Test
    fun `hentEnheter returnerer tom liste`() {
        mockServer.expect(requestTo("/api/v1/enhet"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[]")
            )

        val result = client.hentEnheter()

        assertThat(result).isEmpty()
    }

    @Test
    fun `hentEnheter kaster exception ved 401 Unauthorized`() {
        mockServer.expect(requestTo("/api/v1/enhet"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED))

        assertThatThrownBy { client.hentEnheter() }
            .isInstanceOf(HttpClientErrorException::class.java)
    }

    @Test
    fun `hentTema returnerer sett med tema`() {
        val json = """["HJE", "ORT", "AAP"]"""

        mockServer.expect(requestTo("/api/v1/tema"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
            )

        val result = client.hentTema()

        assertThat(result).hasSize(3)
        assertThat(result).containsExactlyInAnyOrder("HJE", "ORT", "AAP")
    }

    @Test
    fun `hentTema returnerer tomt sett`() {
        mockServer.expect(requestTo("/api/v1/tema"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(
                withStatus(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("[]")
            )

        val result = client.hentTema()

        assertThat(result).isEmpty()
    }

    @Test
    fun `hentTema kaster exception ved 401 Unauthorized`() {
        mockServer.expect(requestTo("/api/v1/tema"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED))

        assertThatThrownBy { client.hentTema() }
            .isInstanceOf(HttpClientErrorException::class.java)
    }

    @Test
    fun `hentTema kaster exception ved 500 Internal Server Error`() {
        mockServer.expect(requestTo("/api/v1/tema"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        assertThatThrownBy { client.hentTema() }
            .isInstanceOf(HttpServerErrorException::class.java)
    }
}
