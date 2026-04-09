package no.nav.historisk.superhelt.infotrygd

import no.nav.common.types.FolkeregisterIdent
import no.nav.infotrygd.InfotrygdClient
import no.nav.infotrygd.InfotrygdHistorikk
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class InfotrygdService(private val infotrygdClient: InfotrygdClient) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @PreAuthorize("hasAuthority('READ') and @tilgangsmaskin.harTilgang(#fnr)")
    fun hentHistorikkFailsafe(fnr: FolkeregisterIdent): List<InfotrygdHistorikk> {
        try {
            return infotrygdClient.hentHistorikk(fnr)
        } catch (ex: Exception) {
            logger.error("Feil ved henting av infotrygd historikk. Return empty list", ex)
            return emptyList()
        }
    }
}
