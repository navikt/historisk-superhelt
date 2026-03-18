package no.nav.historisk.superhelt

import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.enhet.NavEnhetService
import no.nav.historisk.superhelt.infrastruktur.authentication.Role
import no.nav.historisk.superhelt.infrastruktur.authentication.getAuthenticatedUser
import no.nav.historisk.superhelt.infrastruktur.authentication.getCurrentUserRoles
import no.nav.historisk.superhelt.tema.NavTemaService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController(
    private val navEnhetService: NavEnhetService,
    private val navTemaService: NavTemaService,
) {

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getUserInfo(): User {
        val roles = getCurrentUserRoles()
        val user = getAuthenticatedUser().navUser
        return User(
            name = user.navn,
            ident = user.navIdent,
            roles = roles,
            enhet = navEnhetService.hentNavEnhet(),
            tema = navTemaService.hentNavTema(),
        )
    }

    data class User(
        val name: String,
        val ident: NavIdent,
        val roles: List<Role>,
        val enhet: Enhetsnummer,
        val tema: Set<String>,
    )
}

