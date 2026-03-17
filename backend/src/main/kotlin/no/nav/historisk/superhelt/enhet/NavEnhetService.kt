package no.nav.historisk.superhelt.enhet

import no.nav.common.types.Enhetsnummer
import no.nav.entraproxy.EntraProxyClient
import org.springframework.stereotype.Service

@Service
class NavEnhetService(private val entraProxyClient: EntraProxyClient) {

    fun hentNavEnhet(): Enhetsnummer {
        val enheter = entraProxyClient.hentEnheter()
        return enheter.firstOrNull()?.enhetnummer?.let { Enhetsnummer(it) }
            ?: throw IllegalStateException("Fant ingen enhet for innlogget bruker")
    }
}
