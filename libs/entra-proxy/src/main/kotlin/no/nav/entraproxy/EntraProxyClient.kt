package no.nav.entraproxy

import org.springframework.web.client.RestClient
import org.springframework.web.client.body

class EntraProxyClient(private val restClient: RestClient) {

    fun hentEnheter(): List<Enhet> {
        return restClient.get()
            .uri("/api/v1/enhet")
            .retrieve()
            .body<List<Enhet>>() ?: emptyList()
    }
}
