package no.nav.historisk.superhelt.infrastruktur.token


import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse


class NaisTokenClientRequestInterceptor(private val tokenService: NaisTokenService, private val target: String) : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val oboToken = tokenService.oboToken(target)
        request.headers.setBearerAuth(oboToken)
        return execution.execute(request, body)
    }
}