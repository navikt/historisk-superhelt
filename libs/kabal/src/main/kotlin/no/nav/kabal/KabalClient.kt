package no.nav.kabal

import no.nav.kabal.model.SendSakV4Request
import no.nav.kabal.model.SendSakV4Response
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

/**
 * Client for Kabal API (Klage og Anke)
 * Håndterer sending av saker til klage- og ankesystem
 *
 * Krever at header X-Correlation-ID settes i RestClient
 */
class KabalClient(
    private val restClient: RestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendSakV4(request: SendSakV4Request): SendSakV4Response {
        logger.info("Sender sak til Kabal: type=${request.type}, klagerIdent=${request.klager.id.verdi}")
        return restClient.post()
            .uri("/api/oversendelse/v4/sak")
            .body(request)
            .retrieve()
            .body<SendSakV4Response>()
            ?: throw KabalClientException("Tom respons fra Kabal API ved sending av sak")
    }
}

class KabalClientException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

