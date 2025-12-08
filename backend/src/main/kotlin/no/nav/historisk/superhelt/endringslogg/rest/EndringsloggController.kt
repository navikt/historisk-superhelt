package no.nav.historisk.superhelt.endringslogg.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.endringslogg.EndringsloggLinje
import no.nav.historisk.superhelt.endringslogg.EndringsloggService
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.sak.Saksnummer
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak/{saksnummer}/endringslogg")
class EndringsloggController(
    private val endringsloggService: EndringsloggService,
    private val sakRepository: SakRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hent")
    @GetMapping
    fun hentVedtakForSak(@PathVariable saksnummer: Saksnummer): List<EndringsloggLinje> {
        // Henter sak for å verifisere at den eksisterer og sjekke tilgang
        val sak = sakRepository.getSak(saksnummer)
        return endringsloggService.findBySak(sak.saksnummer)
    }

}
