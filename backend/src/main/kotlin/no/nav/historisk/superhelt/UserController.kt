package no.nav.historisk.superhelt

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getUserInfo(principal: Principal): User {
        return User(name=principal.name, roles=listOf("TODO") )
    }
}

data class User(val name: String, val roles: List<String>)