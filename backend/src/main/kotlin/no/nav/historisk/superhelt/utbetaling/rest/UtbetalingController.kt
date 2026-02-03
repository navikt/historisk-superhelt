package no.nav.historisk.superhelt.utbetaling.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.infrastruktur.exception.IkkeFunnetException
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import no.nav.historisk.superhelt.utbetaling.UtbetalingStatus
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/utbetaling")
class UtbetalingController(
    private val utbetalingService: UtbetalingService,
    private val sakRepository: SakRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)


    @Operation(operationId = "retryFeiletUtbetaling", summary = "Kjører på nytt  utbetaling som har feilet")
    @PostMapping("retry/{saksnummer}")
    fun retryFeiletUtbetaling(@PathVariable saksnummer: Saksnummer) {
        logger.info("Retry feilet utbetaling med saksnummer $saksnummer")
        val sak = sakRepository.getSak(saksnummer)
        val utbetaling =
            sak.utbetaling ?: throw IkkeFunnetException("Utbetaling for sak med saksnummer $saksnummer ikke funnet")
        if (utbetaling.utbetalingStatus != UtbetalingStatus.FEILET) {
            throw IllegalStateException("Utbetaling med saksnummer $saksnummer er ikke i FEILET status")
        }
        utbetalingService.retryUtbetaling(sak)
    }
}