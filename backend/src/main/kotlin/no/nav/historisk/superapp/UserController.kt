package no.nav.historisk.superapp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/user")
class UserController {

    @GetMapping
    fun getUserInfo(principal: Principal): String {
        return principal.name
    }
}