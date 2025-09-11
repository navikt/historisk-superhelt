package no.nav.historisk.mock.krr

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.Serializable

@RestController
@RequestMapping("krr-mock")
class KrrController {

    @PostMapping("/rest/v1/personer")
    fun hentKontaktinformasjon(@RequestBody req: PostPersonerRequest) : PostPersonerResponse {
        val personer = req.personidenter.associateWith { DigitalKontaktinfo(spraak = "nb") }
        return PostPersonerResponse(personer)
    }
}

data class PostPersonerRequest(val personidenter: Set<String>)

data class DigitalKontaktinfo(
    val spraak : String? = null,
) : Serializable

data class PostPersonerResponse(
    val personer: Map<String, DigitalKontaktinfo> = mapOf(),
    val feil: Map<String, String> = mapOf(),
)