package no.nav.historisk.mock.entraproxy

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class EnhetResponse(val enhetnummer: String, val navn: String)

@RestController
@RequestMapping("entra-proxy-mock")
class EntraProxyMockController {

    @GetMapping("/api/v1/enhet")
    fun hentEnheter(): List<EnhetResponse> {
        return listOf(
            EnhetResponse(enhetnummer = "4488", navn = "NAV Vest-Viken"),
        )
    }
}
