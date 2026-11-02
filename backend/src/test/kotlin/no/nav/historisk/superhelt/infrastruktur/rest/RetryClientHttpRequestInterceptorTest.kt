package no.nav.historisk.superhelt.infrastruktur.rest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.test.web.client.ExpectedCount.times
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import java.io.IOException
import java.net.SocketTimeoutException

class RetryClientHttpRequestInterceptorTest {
    private val builder = RestClient.builder().baseUrl("https://example.test")
    private val server = MockRestServiceServer.bindTo(builder).build()

    @AfterEach
    fun verifyRequests() {
        server.verify()
    }

    @ParameterizedTest
    @ValueSource(ints = [500, 501, 502, 503, 504])
    fun `5xx is retried and a successful response is returned`(status: Int) {
        val client = client()
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withStatus(HttpStatusCode.valueOf(status)))
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN))

        assertThat(client.get().uri("/resource").retrieve().body(String::class.java)).isEqualTo("ok")
    }

    @ParameterizedTest
    @ValueSource(ints = [200, 201, 302, 400, 401, 403, 404, 409, 410, 429])
    fun `responses outside 5xx are not retried`(status: Int) {
        val client = client()
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withStatus(HttpStatusCode.valueOf(status)))

        val result = client.get().uri("/resource").exchange { _, response -> response.statusCode.value() }

        assertThat(result).isEqualTo(status)
    }

    @Test
    fun `last 5xx reaches normal error handling after three attempts`() {
        val client = client()
        server.expect(times(3), requestTo("https://example.test/resource"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE).body("unavailable"))

        assertThatThrownBy { client.get().uri("/resource").retrieve().toBodilessEntity() }
            .isInstanceOfSatisfying(HttpServerErrorException::class.java) {
                assertThat(it.statusCode).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)
                assertThat(it.responseBodyAsString).isEqualTo("unavailable")
            }
    }

    @Test
    fun `connection errors and 5xx can recover within three attempts`() {
        val client = client()
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withException(IOException("Connection reset")))
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY))
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN))

        assertThat(client.get().uri("/resource").retrieve().body(String::class.java)).isEqualTo("ok")
    }

    @Test
    fun `connection failure reaches the caller after three attempts`() {
        val client = client()
        val failure = IOException("Connection unavailable")
        server.expect(times(3), requestTo("https://example.test/resource"))
            .andRespond(withException(failure))

        assertThatThrownBy { client.get().uri("/resource").retrieve().toBodilessEntity() }
            .isInstanceOf(ResourceAccessException::class.java)
            .hasCause(failure)
    }

    @Test
    fun `socket timeout is retried`() {
        val client = client()
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withException(SocketTimeoutException()))
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN))

        assertThat(client.get().uri("/resource").retrieve().body(String::class.java)).isEqualTo("ok")
    }

    @ParameterizedTest
    @CsvSource(
        "POST, /api/v1/oppgaver, 1",
        "GET, /api/v1/oppgaver, 3",
        "PATCH, /api/v1/oppgaver/123, 3",
        "POST, /other, 3",
    )
    fun `filter excludes only the selected operation`(httpMethod: String, path: String, expectedCalls: Int) {
        val client = client { !(it.method == HttpMethod.POST && it.uri.path == "/api/v1/oppgaver") }
        server.expect(times(expectedCalls), requestTo("https://example.test$path"))
            .andExpect(method(HttpMethod.valueOf(httpMethod)))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        assertThatThrownBy {
            client.method(HttpMethod.valueOf(httpMethod)).uri(path).retrieve().toBodilessEntity()
        }.isInstanceOf(HttpServerErrorException::class.java)
    }

    @Test
    fun `excluded request does not retry connection failure`() {
        val client = client { false }
        val failure = IOException("Connection reset")
        server.expect(requestTo("https://example.test/resource"))
            .andRespond(withException(failure))

        assertThatThrownBy { client.post().uri("/resource").retrieve().toBodilessEntity() }
            .isInstanceOf(ResourceAccessException::class.java)
            .hasCause(failure)
    }

    private fun client(shouldRetry: (HttpRequest) -> Boolean = { true }): RestClient =
        builder.requestInterceptor(RetryClientHttpRequestInterceptor(shouldRetry = shouldRetry, sleep = {})).build()
}
