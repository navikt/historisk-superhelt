package no.nav.historisk.superhelt.infotrygd

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.infotrygd.InfotrygdHistorikk
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/infotrygd")
class InfotrygdController(private val infotrygdService: InfotrygdService) {

    @Operation(operationId = "hentInfotrygdHistorikkForPerson")
    @GetMapping("historikk/{maskertPersonIdent}")
    fun hentHistorikkForPerson(@PathVariable maskertPersonIdent: MaskertPersonIdent): List<InfotrygdHistorikk> {
        return infotrygdService.hentHistorikkFailsafe(maskertPersonIdent.toFnr())
    }
}
