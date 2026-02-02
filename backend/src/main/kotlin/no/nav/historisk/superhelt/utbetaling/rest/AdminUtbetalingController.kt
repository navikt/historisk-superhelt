package no.nav.historisk.superhelt.utbetaling.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.historisk.superhelt.infrastruktur.Permission
import no.nav.historisk.superhelt.infrastruktur.permission.SecurityContextUtils
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import no.nav.historisk.superhelt.utbetaling.UtbetalingService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin", description = "Admin API for utbetalinger")
@RestController
@RequestMapping("/admin/utbetaling")
class AdminUtbetalingController(
    private val utbetalingRepository: UtbetalingRepository,
    private val utbetalingService: UtbetalingService,
    private val sakRepository: SakRepository,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hentFeileteUtbetalinger", summary = "Hent alle utbetalinger som har feilet")
    @GetMapping("feilet")
    fun hentFeiledeUtbetalinger(): List<Utbetaling> {
        return utbetalingRepository.findUtbetalingerFeilet()
    }

    @Operation(operationId = "rekjorFeileteUtbetalinger", summary = "Kjører på nytt  utbetalinger som har feilet")
    @PostMapping("feilet")
    fun retryFeileteUtbetalinger(@RequestBody(required = false) request: RetryUtbetalingRequestDto?): List<Utbetaling> {
        val findUtbetalingerFeilet = utbetalingRepository.findUtbetalingerFeilet()

        val utbetalingerToRetry = request?.utbetalingIds?.let { ider ->
            findUtbetalingerFeilet.filter { it.uuid in ider }
        } ?: findUtbetalingerFeilet

        logger.info(
            "Kjører på nytt feilete utbetalinger {}",
            utbetalingerToRetry.map { "${it.saksnummer}: ${it.uuid}" })

        SecurityContextUtils.runWithPermissons(listOf(Permission.READ, Permission.WRITE, Permission.IGNORE_TILGANGSMASKIN)) {
            utbetalingerToRetry.forEach {
                val sak = sakRepository.getSak(it.saksnummer)
                utbetalingService.retryUtbetaling(sak)
            }
        }
        return utbetalingerToRetry

    }


}