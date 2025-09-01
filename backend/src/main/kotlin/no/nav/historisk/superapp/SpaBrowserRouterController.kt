package no.nav.historisk.superapp

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/*
    Forward alle requests for frontend til index.html slik at browser kan lastes på nytt og linkes inn til
 */

@Controller
 class SpaBrowserRouterController {

    @GetMapping("{path:(?!assets|swagger-ui)[^.]*}/**")
    fun handleFrontendPaths(): String {
        return "forward:/"
    }
}
