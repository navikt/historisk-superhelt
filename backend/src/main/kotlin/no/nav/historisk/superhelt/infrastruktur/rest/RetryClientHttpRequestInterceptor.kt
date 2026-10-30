package no.nav.historisk.superhelt.infrastruktur.rest

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.backoff.ExponentialBackOff
import java.io.IOException
import java.io.InterruptedIOException
import java.nio.channels.ClosedByInterruptException
import java.time.Duration

/**
 * Gjentar kall ved 5xx og I/O-feil med eksponentiell backoff og jitter, uten å lese responsbody.
 *
 * Trådavbrudd stopper retry. Ved avbrutt venting gjenopprettes interrupt-flagget slik at
 * kalleren kan oppdage avbruddet. Socket-timeout uten trådavbrudd får fortsatt retry.
 */
class RetryClientHttpRequestInterceptor(
    private val maxAttempts: Int = 3,
    initialDelay: Duration = Duration.ofMillis(200),
    multiplier: Double = 2.0,
    jitter: Duration = Duration.ofMillis(50),
    maxDelay: Duration = Duration.ofSeconds(1),
    private val sleep: (Long) -> Unit = { Thread.sleep(it) },
) : ClientHttpRequestInterceptor {

    init {
        require(maxAttempts >= 1) { "maxAttempts must be at least 1" }
        require(!initialDelay.isNegative) { "initialDelay must be positive" }
        require( multiplier >= 1) { "multiplier must be at least 1" }
        require(!jitter.isNegative) { "jitter must be positive" }
        require(maxDelay >= initialDelay) { "maxDelay must not be less than initialDelay" }
    }

    private val backOff = ExponentialBackOff(initialDelay.toMillis(), multiplier).apply {
        setJitter(jitter.toMillis())
        maxInterval = maxDelay.toMillis()
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val delays = backOff.start()
        var attempt = 1
        while (true) {
            checkInterrupted()
            val lastAttempt = attempt == maxAttempts
            try {
                val response = execution.execute(request, body)
                if (!response.isServerError() || lastAttempt) {
                    return response
                }
                response.close()
            } catch (exception: IOException) {
                if (exception is ClosedByInterruptException) {
                    Thread.currentThread().interrupt()
                }
                if (Thread.currentThread().isInterrupted || lastAttempt) {
                    throw exception
                }
            }
            pause(delays.nextBackOff())
            attempt++
        }
    }

    private fun ClientHttpResponse.isServerError(): Boolean {
        try {
            return statusCode.is5xxServerError
        } catch (exception: IOException) {
            close()
            throw exception
        }
    }

    private fun pause(delay: Long) {
        checkInterrupted()
        try {
            sleep(delay)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw InterruptedIOException("HTTP retry interrupted").apply { initCause(exception) }
        }
    }

    private fun checkInterrupted() {
        if (Thread.currentThread().isInterrupted) {
            throw InterruptedIOException("HTTP retry interrupted")
        }
    }
}
