package no.nav.historisk.superhelt.auth.token

import no.nav.historisk.superhelt.auth.getCurrentUserToken
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClient.builder

//TODO cache og retry logikk
class NaisTokenService(
    oboEndpoint: String,
    m2mEndpoint: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val texas = NaisTokenClient(
        client = builder().build(),
        oboEndpoint = oboEndpoint,
        m2mEndpoint = m2mEndpoint
    )

    init {
        log.debug("NaisTokenService satt opp med oboEndpoint=$oboEndpoint og m2mEndpoint=$m2mEndpoint")
    }

    /** Henter token for innlogget bruker */
    fun oboToken(target: String): String {
        getCurrentUserToken()?.let { token ->
            return texas.oboToken(target, token).access_token
        }
        throw IllegalArgumentException("Kunne ikke hente token for innlogget bruker")
    }

    /** Henter token for denne applikajsonen */
    fun m2mToken(target: String): String {
        return texas.m2mToken(target).access_token

    }


}