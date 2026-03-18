package no.nav.historisk.superhelt.tema

import no.nav.entraproxy.EntraProxyClient
import org.springframework.stereotype.Service

@Service
class NavTemaService(private val entraProxyClient: EntraProxyClient) {

    fun hentNavTema(): Set<String> {
        return entraProxyClient.hentTema()
    }
}
