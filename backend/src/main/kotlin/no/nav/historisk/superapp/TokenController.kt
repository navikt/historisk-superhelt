package no.nav.historisk.superapp

import no.nav.historisk.superapp.auth.token.NaisTokenService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * For verifisering av tokens.
 * TODO fjerne denne når alt er verifisert
 */
@RestController
@RequestMapping("/api/token")
class TokenController(private val tokenService: NaisTokenService) {

    @GetMapping("obo")
    fun obotoken(): String {
        return tokenService.oboToken("api://dev-gcp.historisk.historisk-helt-infotrygd/.default")
    }

    @GetMapping("m2m")
    fun m2mtoken(): String {
        return tokenService.m2mToken("api://dev-gcp.historisk.historisk-helt-infotrygd/.default")
    }


}