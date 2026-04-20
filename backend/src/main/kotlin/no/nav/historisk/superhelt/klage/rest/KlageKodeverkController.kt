package no.nav.historisk.superhelt.klage.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.kabal.model.Hjemmel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/klage/kodeverk")
class KlageKodeverkController {

    @Operation(operationId = "getKodeverkHjemler")
    @GetMapping("hjemler")
    fun hjemlerKodeverk(): List<HjemmelDto> {
        return Hjemmel.entries.map { hjemmel ->
            HjemmelDto(
                id = hjemmel.id,
                lovKildeNavn = hjemmel.lovKilde.navn,
                lovKildeBeskrivelse = hjemmel.lovKilde.beskrivelse,
                spesifikasjon = hjemmel.spesifikasjon,
                visningsnavn = "${hjemmel.lovKilde.beskrivelse} ${hjemmel.spesifikasjon}",
            )
        }
    }
}

