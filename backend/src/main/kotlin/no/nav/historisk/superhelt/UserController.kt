package no.nav.historisk.superhelt

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getUserInfo(auth: Authentication): User {
        return User(name=auth.name, roles=auth.authorities.map { it.authority } )
    }
    @PreAuthorize("hasRole('SAKSBEHANDLER')")
    @GetMapping(path = ["/v2"])
    fun getUserInfo2(auth: Authentication): User {
        return User(name=auth.name, roles=auth.authorities.map { it.authority } )
    }
    @PreAuthorize("hasRole('LES')")
    @GetMapping(path = ["/v3"])
    fun getUserInfo3(auth: Authentication): User {
        return User(name=auth.name, roles=auth.authorities.map { it.authority } )
    }
}

data class User(val name: String, val roles: List<String>)