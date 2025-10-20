package no.nav.historisk.superhelt

import no.nav.historisk.superhelt.infrastruktur.Role
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
        return User(name=auth.name, roles= roles )
    }
}

data class User(val name: String, val roles: List<Role>)