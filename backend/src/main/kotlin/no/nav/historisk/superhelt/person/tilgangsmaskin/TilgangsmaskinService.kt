package no.nav.historisk.superhelt.person.tilgangsmaskin

import no.nav.person.Fnr
import no.nav.tilgangsmaskin.TilgangsmaskinClient


class TilgangsmaskinService(
    private val tilgangsmaskinClient: TilgangsmaskinClient,
) {

    //TODO caching?
    fun sjekkKomplettTilgang(fnr: Fnr): TilgangsmaskinClient.TilgangResult {
        return tilgangsmaskinClient.komplett(fnr.value)
    }

}

