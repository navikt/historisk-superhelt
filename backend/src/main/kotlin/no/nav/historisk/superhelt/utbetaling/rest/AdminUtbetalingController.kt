package no.nav.historisk.superhelt.utbetaling.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.historisk.superhelt.utbetaling.Utbetaling
import no.nav.historisk.superhelt.utbetaling.UtbetalingRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name="Admin", description = "Admin API for utbetalinger")
@RestController
@RequestMapping("/admin/utbetaling")
class AdminUtbetalingController(private val utbetalingRepository: UtbetalingRepository,) {

    @Operation(operationId = "feileteUtbetalinger", summary = "Hent alle utbetalinger som har feilet")
    @GetMapping("feilet")
    fun hentFeiledeUtbetalinger(): List<Utbetaling> {
        val findUtbetalingerFeilet = utbetalingRepository.findUtbetalingerFeilet()
        return findUtbetalingerFeilet
    }

}