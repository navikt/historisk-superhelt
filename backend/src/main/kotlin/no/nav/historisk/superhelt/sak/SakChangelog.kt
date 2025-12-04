package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SakChangelog {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun logChange(saksnummer: Saksnummer, endring: String) {
        val navBruker = getCurrentNavIdent()
        logger.info("CHANGELOG: Sak $saksnummer endret: $endring av $navBruker" )
        // lagre i database
    }
}