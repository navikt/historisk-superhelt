package no.nav.historisk.superhelt.klage.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.StonadsType
import no.nav.historisk.superhelt.klage.kabalYtelse
import no.nav.kabal.model.KabalHjemmel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/klage/kodeverk")
class KlageKodeverkController {

    /** Returnerer alle hjemler på tvers av ytelser – brukes primært til generell oppslag. */
    @Operation(operationId = "getKodeverkHjemler")
    @GetMapping("hjemler")
    fun hjemlerKodeverk(): List<HjemmelDto> {
        return KabalHjemmel.entries.map { it.toDto() }
    }

    /**
     * Returnerer kun hjemler som er gyldige for en stønad.
     */
    @Operation(operationId = "getKodeverkHjemlerForStonad")
    @GetMapping("hjemler/{stonadsType}")
    fun hjemlerForStonad(@PathVariable stonadsType: StonadsType): List<HjemmelDto> {
        return KabalHjemmel.forYtelse(stonadsType.kabalYtelse).map { it.toDto() }
    }

    /** Konverterer et Hjemmel-enum til DTO for REST-responsen. */
    private fun KabalHjemmel.toDto() = HjemmelDto(
        id = id,
        lovKildeNavn = lovKilde.navn,
        lovKildeBeskrivelse = lovKilde.beskrivelse,
        spesifikasjon = spesifikasjon,
        visningsnavn = "${lovKilde.beskrivelse} $spesifikasjon",
    )
}

