package no.nav.historisk.superhelt.infotrygd

import no.nav.common.types.FolkeregisterIdent
import no.nav.entraproxy.InfotrygdClient
import no.nav.infotrygd.InfotrygdHistorikk
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class InfotrygdService(private val infotrygdClient: InfotrygdClient) {

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun hentHistorikk(fnr: FolkeregisterIdent): List<InfotrygdHistorikk> {
        return infotrygdClient.hentHistorikk(fnr)
    }
}
