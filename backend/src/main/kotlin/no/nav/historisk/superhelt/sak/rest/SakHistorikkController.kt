package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.infotrygd.InfotrygdService
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.infotrygd.InfotrygdHistorikk
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sakhistorikk")
class SakHistorikkController(private val infotrygdService: InfotrygdService, private val sakRepository: SakRepository) {

    @Operation(operationId = "hentSakHistorikkForPerson")
    @GetMapping("/person/{maskertPersonIdent}/{tema}")
    fun hentSakHistorikkForPerson(
        @PathVariable maskertPersonIdent: MaskertPersonIdent,
        @PathVariable tema: FellesKodeverkTema): SakHistorikkResponse {

        if (!getAuthenticatedUser().hasTemaAccess(tema)) {
            return SakHistorikkResponse(emptyList())
        }

        val saker = sakRepository.finnSaker(maskertPersonIdent.toFnr()).filter { it.type.tema == tema }
        if (tema == FellesKodeverkTema.HEL) {
            return SakHistorikkResponse(
                saker = saker,
                infotrygd = infotrygdService.hentHistorikkFailsafe(maskertPersonIdent.toFnr())
            )
        }
        return SakHistorikkResponse(
            saker = saker,
        )
    }

    data class SakHistorikkResponse(
        val saker: List<Sak>,
        val infotrygd: List<InfotrygdHistorikk> = emptyList()
    )

}
