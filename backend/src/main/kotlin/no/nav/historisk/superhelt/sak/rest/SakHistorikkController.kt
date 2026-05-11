package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.common.consts.FellesKodeverkTema
import no.nav.historisk.superhelt.infotrygd.InfotrygdService
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakRepository
import no.nav.infotrygd.InfotrygdHistorikk
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sakhistorikk")
class SakHistorikkController(private val infotrygdService: InfotrygdService, private val sakRepository: SakRepository) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Operation(operationId = "hentSakHistorikkForPerson")
    @GetMapping("/person/{maskertPersonIdent}")
    fun hentSakHistorikkForPerson(
        @PathVariable maskertPersonIdent: MaskertPersonIdent,
        @RequestParam tema: FellesKodeverkTema?): SakHistorikkResponse {

        val fnr = maskertPersonIdent.toFnr()
        val authenticatedUser = getAuthenticatedUser()
        val temaer: List<FellesKodeverkTema> = when {
            tema != null && !authenticatedUser.hasTemaAccess(tema) -> emptyList()
            tema != null -> listOf(tema)
            else -> authenticatedUser.tema
        }

        if (temaer.isEmpty()) {
            logger.debug("Bruker har ikke tilgang til noen tema. Gir tom historikk")
            return SakHistorikkResponse(emptyList())
        }

        val saker = sakRepository.finnSaker(fnr).filter { tema == null || it.tema == tema }
        val infotrygd = if (temaer.contains(FellesKodeverkTema.HEL)) infotrygdService.hentHistorikkFailsafe(fnr) else emptyList()

        return SakHistorikkResponse(saker = saker, infotrygd = infotrygd)
    }

    data class SakHistorikkResponse(
        val saker: List<Sak>,
        val infotrygd: List<InfotrygdHistorikk> = emptyList()
    )

}
