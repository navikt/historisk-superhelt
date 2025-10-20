package no.nav.historisk.superhelt.infrastruktur.mdc


import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.util.*



fun callIdFromMDC() = MdcHelper.callId ?: UUID.randomUUID().toString()

class CallIdClientRequestInterceptor(private val headerName: String= "Nav-Call-Id") : ClientHttpRequestInterceptor {

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val callId: String = callIdFromMDC()
        request.headers[headerName] = callId
        return execution.execute(request, body)
    }
}