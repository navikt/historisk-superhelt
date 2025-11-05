package no.nav.historisk.superhelt.sak

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SakChangelog {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun logChange(saksnummer: Saksnummer, endring: String) {
        logger.info("CHANGELOG: Sak $saksnummer endret: $endring")
        // lagre i database
    }
}