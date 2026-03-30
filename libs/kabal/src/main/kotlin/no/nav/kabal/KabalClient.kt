package no.nav.kabal

import no.nav.kabal.model.SendSakV4Request
import no.nav.kabal.model.SendSakV4Response
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

/**
 * Klient for Kabal API (Klage og Anke)
 * Håndterer sending av saker til klage- og ankesystem
 *
 * Krever at header X-Correlation-ID settes i RestClient
 */
class KabalClient(
    private val restClient: RestClient,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendSakV4(request: SendSakV4Request): SendSakV4Response {
        return try {
            restClient.post()
                .uri("/api/oversendelse/v4/sak")
                .body(request)
                .retrieve()
                .body<SendSakV4Response>()
                ?: throw KabalException("Tom respons fra Kabal API ved sending av sak")
        } catch (e: RestClientResponseException) {
            logger.error("Feil fra Kabal API: HTTP ${e.statusCode} – ${e.responseBodyAsString}")
            throw KabalException(
                message = "Feil fra Kabal API: HTTP ${e.statusCode}",
                cause = e,
                statusCode = e.statusCode.value(),
                responseBody = e.responseBodyAsString,
            )
        }
    }
}

