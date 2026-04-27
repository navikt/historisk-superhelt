package no.nav.historisk.superhelt.ansatt.rest

import no.nav.historisk.superhelt.ansatt.NavAnsatt
import no.nav.historisk.superhelt.ansatt.NavAnsattService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class NavAnsattController(
    private val navAnsattService: NavAnsattService,
) {

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getUserInfo(): NavAnsatt {
       return navAnsattService.hentNavAnsatt()
    }
}
