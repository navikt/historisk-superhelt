package no.nav.historisk.mock.infotrygd

import no.nav.common.types.FolkeregisterIdent
import no.nav.infotrygd.InfotrygdHistorikkRequest
import no.nav.infotrygd.InfotrygdHistorikkResponse
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("infotrygd-mock")
class InfotrygdMockController {
    private val repository = mutableMapOf<FolkeregisterIdent, InfotrygdHistorikkResponse>()

    private val logger = LoggerFactory.getLogger(javaClass)

    @PostMapping("/api/hentData")
    fun infotrygdHistorikk(@RequestBody req: InfotrygdHistorikkRequest): InfotrygdHistorikkResponse {
        val fnr = req.fnr.firstOrNull() ?: throw IllegalStateException("Mangler fnr")

        return repository[fnr] ?: genererOgLagre(fnr)
    }

    private fun genererOgLagre(fnr: FolkeregisterIdent): InfotrygdHistorikkResponse {
        val response = genererInfotrygdHistorikkResponse()
        repository[fnr] = response
        logger.debug("Genererte infotrygd historikk for fnr: {}, antall treff: {}", fnr, response.personkort.size)
        return response
    }
}
