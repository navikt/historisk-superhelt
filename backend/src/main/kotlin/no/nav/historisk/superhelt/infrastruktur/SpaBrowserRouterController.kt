package no.nav.historisk.superhelt.infrastruktur

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping


@Controller
 class SpaBrowserRouterController {

    /**
    Forward alle requests for frontend til index.html slik at browser kan lastes på nytt og linkes inn til
     */

    @GetMapping("{path:(?!assets|swagger-ui)[^.]*}/**")
    fun handleFrontendPaths(): String {
        return "forward:/"
    }
}