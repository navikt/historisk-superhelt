package no.nav.historisk.superhelt.person.tilgangsmaskin

import no.nav.common.types.FolkeregisterIdent
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.tilgangsmaskin.TilgangsmaskinClient
import org.springframework.cache.Cache


class TilgangsmaskinService(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
    private val cache: Cache
) {

    fun sjekkKomplettTilgang(fnr: FolkeregisterIdent): TilgangsmaskinClient.TilgangResult {
        val cacheKey = "${getAuthenticatedUser().navIdent}:${fnr.value}"

        return cache.get(cacheKey, TilgangsmaskinClient.TilgangResult::class.java)
            ?: tilgangsmaskinClient.komplett(fnr.value).also { result ->
                cache.put(cacheKey, result)
            }
    }
}

