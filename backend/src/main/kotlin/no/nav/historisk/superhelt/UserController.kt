package no.nav.historisk.superhelt

import no.nav.common.types.NavIdent
import no.nav.historisk.superhelt.infrastruktur.Role
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavUser
import no.nav.historisk.superhelt.infrastruktur.getCurrentUserRoles
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    fun getUserInfo(auth: Authentication): User {
        val roles = getCurrentUserRoles()
        val user = getCurrentNavUser()
        return User(name=user.navn, ident = user.navIdent, roles= roles )
    }
    
    data class User(val name: String, val ident: NavIdent, val roles: List<Role>)
}

