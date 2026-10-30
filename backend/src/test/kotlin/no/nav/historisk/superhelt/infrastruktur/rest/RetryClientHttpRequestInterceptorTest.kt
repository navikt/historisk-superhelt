package no.nav.historisk.superhelt.infrastruktur.rest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpResponse
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.URI
import java.nio.channels.ClosedByInterruptException
import java.time.Duration
import java.util.stream.IntStream

class RetryClientHttpRequestInterceptorTest {
    private val request = MockClientHttpRequest(HttpMethod.POST, URI("https://example.test/resource"))
    private val body = "request-body".toByteArray()
    private val pauses = mutableListOf<Long>()
    private val interceptor = RetryClientHttpRequestInterceptor(
        jitter = Duration.ZERO,
        sleep = { pauses.add(it) },
    )

    @AfterEach
    fun clearInterrupt() {
        Thread.interrupted()
    }

    @ParameterizedTest
    @MethodSource("serverErrors")
    fun `every 5xx is retried and the failed response is closed`(status: Int) {
        val failed = TrackingResponse(status)
        val success = TrackingResponse(200)
        var calls = 0

        val result = intercept {
            calls++
            if (calls == 1) failed else {
                assertThat(failed.closed).isTrue()
                success
            }
        }

        assertThat(result).isSameAs(success)
        assertThat(calls).isEqualTo(2)
        assertThat(pauses).containsExactly(200L)
        assertThat(success.closed).isFalse()
        assertThat(failed.bodyAccessed).isFalse()
    }

    @ParameterizedTest
    @MethodSource("nonServerStatuses")
    fun `responses outside 5xx are returned without retry or body access`(status: Int) {
        val response = TrackingResponse(status)
        var calls = 0

        assertThat(intercept { calls++; response }).isSameAs(response)

        assertThat(calls).isEqualTo(1)
        assertThat(pauses).isEmpty()
        assertThat(response.closed).isFalse()
        assertThat(response.bodyAccessed).isFalse()
    }

    @Test
    fun `last 5xx remains open for normal status handling after three attempts`() {
        val responses = List(3) { TrackingResponse(503) }
        var calls = 0

        val result = intercept { responses[calls++] }

        assertThat(result).isSameAs(responses.last())
        assertThat(calls).isEqualTo(3)
        assertThat(responses.take(2)).allSatisfy { assertThat(it.closed).isTrue() }
        assertThat(responses.last().closed).isFalse()
        assertThat(pauses).containsExactly(200L, 400L)
    }

    @Test
    fun `I O errors and 5xx share the same attempt budget and request body`() {
        val failed = TrackingResponse(502)
        val success = TrackingResponse(200)
        var calls = 0

        val result = interceptor.intercept(request, body) { actualRequest, actualBody ->
            assertThat(actualRequest).isSameAs(request)
            assertThat(actualBody).isSameAs(body)
            when (++calls) {
                1 -> throw IOException("Connection reset")
                2 -> failed
                else -> success
            }
        }

        assertThat(result).isSameAs(success)
        assertThat(calls).isEqualTo(3)
        assertThat(failed.closed).isTrue()
        assertThat(pauses).containsExactly(200L, 400L)
    }

    @Test
    fun `last I O error is propagated after three attempts`() {
        val failure = IOException("Connection unavailable")
        var calls = 0

        assertThatThrownBy { intercept { calls++; throw failure } }.isSameAs(failure)

        assertThat(calls).isEqualTo(3)
        assertThat(pauses).containsExactly(200L, 400L)
    }

    @Test
    fun `response is closed when reading its status fails`() {
        val failed = object : TrackingResponse(200) {
            override fun getStatusCode(): HttpStatusCode = throw IOException("Status unavailable")
        }
        val success = TrackingResponse(200)
        var calls = 0

        assertThat(intercept { if (++calls == 1) failed else success }).isSameAs(success)

        assertThat(failed.closed).isTrue()
        assertThat(calls).isEqualTo(2)
    }

    @Test
    fun `body read failures after returning the response are not retried`() {
        val response = object : TrackingResponse(200) {
            override fun getBody(): InputStream = object : InputStream() {
                override fun read(): Int = throw IOException("Body connection reset")
            }
        }
        var calls = 0
        val result = intercept { calls++; response }

        assertThatThrownBy { result.body.read() }.isInstanceOf(IOException::class.java)

        assertThat(calls).isEqualTo(1)
        assertThat(pauses).isEmpty()
    }

    @Test
    fun `backoff doubles and is capped without delaying after the last attempt`() {
        val retry = RetryClientHttpRequestInterceptor(
            maxAttempts = 5,
            jitter = Duration.ZERO,
            sleep = { pauses.add(it) },
        )
        var calls = 0

        retry.intercept(request, body) { _, _ -> calls++; TrackingResponse(503) }

        assertThat(calls).isEqualTo(5)
        assertThat(pauses).containsExactly(200L, 400L, 800L, 1000L)
    }

    @Test
    fun `one attempt disables retry`() {
        val retry = RetryClientHttpRequestInterceptor(maxAttempts = 1, sleep = { pauses.add(it) })
        val response = TrackingResponse(503)
        var calls = 0

        assertThat(retry.intercept(request, body) { _, _ -> calls++; response }).isSameAs(response)

        assertThat(calls).isEqualTo(1)
        assertThat(response.closed).isFalse()
        assertThat(pauses).isEmpty()
    }

    @Test
    fun `each invocation has its own backoff and attempt budget`() {
        repeat(2) {
            var calls = 0
            intercept { if (++calls < 3) TrackingResponse(503) else TrackingResponse(200) }
            assertThat(calls).isEqualTo(3)
        }

        assertThat(pauses).containsExactly(200L, 400L, 200L, 400L)
    }

    @Test
    fun `non I O failures are not retried`() {
        val failure = IllegalStateException("Unexpected transport failure")
        var calls = 0

        assertThatThrownBy { intercept { calls++; throw failure } }.isSameAs(failure)

        assertThat(calls).isEqualTo(1)
        assertThat(pauses).isEmpty()
    }

    @Test
    fun `socket timeout is retried without treating it as thread interruption`() {
        var calls = 0

        intercept { if (++calls == 1) throw SocketTimeoutException() else TrackingResponse(200) }

        assertThat(calls).isEqualTo(2)
        assertThat(Thread.currentThread().isInterrupted).isFalse()
    }

    @Test
    fun `interrupted thread does not start a request`() {
        var calls = 0
        Thread.currentThread().interrupt()

        assertThatThrownBy { intercept { calls++; TrackingResponse(200) } }
            .isInstanceOf(InterruptedIOException::class.java)

        assertThat(calls).isZero()
        assertThat(pauses).isEmpty()
        assertThat(Thread.currentThread().isInterrupted).isTrue()
    }

    @Test
    fun `transport interruption reported as I O error is not retried`() {
        val failure = IOException("Transport interrupted")
        var calls = 0

        assertThatThrownBy {
            intercept {
                calls++
                Thread.currentThread().interrupt()
                throw failure
            }
        }.isSameAs(failure)

        assertThat(calls).isEqualTo(1)
        assertThat(pauses).isEmpty()
        assertThat(Thread.currentThread().isInterrupted).isTrue()
    }

    @Test
    fun `closed by interrupt is not retried and preserves interruption`() {
        var calls = 0

        assertThatThrownBy { intercept { calls++; throw ClosedByInterruptException() } }
            .isInstanceOf(ClosedByInterruptException::class.java)

        assertThat(calls).isEqualTo(1)
        assertThat(pauses).isEmpty()
        assertThat(Thread.currentThread().isInterrupted).isTrue()
    }

    @Test
    fun `interruption during backoff stops retry and closes the failed response`() {
        val interruption = InterruptedException("Sleep interrupted")
        val retry = RetryClientHttpRequestInterceptor(sleep = { throw interruption })
        val response = TrackingResponse(503)
        var calls = 0

        assertThatThrownBy { retry.intercept(request, body) { _, _ -> calls++; response } }
            .isInstanceOf(InterruptedIOException::class.java)
            .hasCause(interruption)

        assertThat(calls).isEqualTo(1)
        assertThat(response.closed).isTrue()
        assertThat(Thread.currentThread().isInterrupted).isTrue()
    }

    private fun intercept(execute: () -> ClientHttpResponse): ClientHttpResponse =
        interceptor.intercept(request, body, ClientHttpRequestExecution { _, _ -> execute() })

    private open class TrackingResponse(status: Int) :
        MockClientHttpResponse("response-body".toByteArray(), HttpStatusCode.valueOf(status)) {
        var closed = false
        var bodyAccessed = false

        override fun close() {
            closed = true
            super.getBody().close()
        }

        override fun getBody(): InputStream {
            bodyAccessed = true
            return super.getBody()
        }
    }

    companion object {
        @JvmStatic
        fun serverErrors(): IntStream = IntStream.rangeClosed(500, 504)

        @JvmStatic
        fun nonServerStatuses(): IntStream = IntStream.of(200, 201, 302,  400, 401, 403, 404, 409, 410, 429)
    }
}
