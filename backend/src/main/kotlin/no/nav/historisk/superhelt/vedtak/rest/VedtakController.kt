package no.nav.historisk.superhelt.vedtak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.sak.SakExtensions.auditLog
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.vedtak.Vedtak
import no.nav.historisk.superhelt.vedtak.VedtakRepository
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak/{saksnummer}/vedtak")
class VedtakController(
    private val vedtakRepository: VedtakRepository,
    private val sakRepository: SakRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hentVedtakForSak")
    @GetMapping
    fun hentVedtakForSak(@PathVariable saksnummer: Saksnummer): List<Vedtak> {
        val sak = sakRepository.getSak(saksnummer)
        sak.auditLog("Hener opp vedtak for sak")
        logger.info("Henter vedtak for sak $saksnummer")
        return vedtakRepository.findBySak(sak.saksnummer)
    }


}
