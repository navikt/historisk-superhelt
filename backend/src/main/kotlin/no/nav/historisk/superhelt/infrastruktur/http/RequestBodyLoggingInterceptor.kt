package no.nav.historisk.superhelt.infrastruktur.http

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class RequestBodyLoggingInterceptor : ClientHttpRequestInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        logger.debug(
            "[DEV] {} {}\nBody: {}",
            request.method,
            request.uri,
            body.toString(Charsets.UTF_8),
        )
        return execution.execute(request, body)
    }
}

