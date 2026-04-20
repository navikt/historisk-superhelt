package no.nav.kabal

import no.nav.kabal.model.SendSakV4Request
import org.springframework.web.client.RestClient

/**
 * Klient for Kabal API (Klage og Anke)
 * Håndterer sending av saker til klage- og ankesystem
 *
 * Krever at header X-Correlation-ID settes i RestClient
 */
class KabalClient(
    private val restClient: RestClient,
) {


    /**
     * Sender sak til Kabal API v4.
     * Kabal returnerer ingen body (void-endepunkt) – suksess = ingen exception.
     */
    fun sendSakV4(request: SendSakV4Request) {
        restClient.post()
            .uri("/api/oversendelse/v4/sak")
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }
}



