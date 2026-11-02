package no.nav.historisk.superhelt.infrastruktur.rest

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.http.client.MockClientHttpRequest
import org.springframework.mock.http.client.MockClientHttpResponse
import java.io.InterruptedIOException
import java.net.URI
import java.time.Duration

class RetryClientHttpRequestInterceptorBackoffTest {
    private val request = MockClientHttpRequest(HttpMethod.GET, URI("https://example.test/resource"))

    @AfterEach
    fun clearInterrupt() {
        Thread.interrupted()
    }

    @Test
    fun `backoff doubles between attempts without waiting after the last attempt`() {
        val pauses = mutableListOf<Long>()
        val interceptor = RetryClientHttpRequestInterceptor(jitter = Duration.ZERO, sleep = { pauses.add(it) })

        interceptor.intercept(request, byteArrayOf()) { _, _ ->
            MockClientHttpResponse(byteArrayOf(), HttpStatus.SERVICE_UNAVAILABLE)
        }.close()

        assertThat(pauses).containsExactly(200L, 400L)
    }

    @Test
    fun `interrupted waiting stops retry and preserves the interrupt flag`() {
        val interceptor = RetryClientHttpRequestInterceptor(sleep = { throw InterruptedException() })
        var calls = 0

        assertThatThrownBy {
            interceptor.intercept(request, byteArrayOf()) { _, _ ->
                calls++
                MockClientHttpResponse(byteArrayOf(), HttpStatus.SERVICE_UNAVAILABLE)
            }
        }.isInstanceOf(InterruptedIOException::class.java)

        assertThat(calls).isEqualTo(1)
        assertThat(Thread.currentThread().isInterrupted).isTrue()
    }
}
