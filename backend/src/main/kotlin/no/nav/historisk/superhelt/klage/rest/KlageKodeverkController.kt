package no.nav.historisk.superhelt.klage.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.kabal.model.Hjemmel
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
        return Hjemmel.entries.map { it.toDto() }
    }

    /**
     * Returnerer kun hjemler som er gyldige for den angitte Kabal-ytelseId-en.
     * Ytelsefiltrering sørger for at saksbehandler bare ser relevante hjemler
     * for sakens stønadstype (f.eks. kun ortopedi-hjemler for HEL_HEL,
     * hjelpemiddelhjemler for HJE_HJE, og AUR-hjemler for HJE_AUR).
     */
    @Operation(operationId = "getKodeverkHjemlerForYtelse")
    @GetMapping("hjemler/{ytelseId}")
    fun hjemlerForYtelse(@PathVariable ytelseId: String): List<HjemmelDto> {
        return Hjemmel.forYtelse(ytelseId).map { it.toDto() }
    }

    /** Konverterer et Hjemmel-enum til DTO for REST-responsen. */
    private fun Hjemmel.toDto() = HjemmelDto(
        id = id,
        lovKildeNavn = lovKilde.navn,
        lovKildeBeskrivelse = lovKilde.beskrivelse,
        spesifikasjon = spesifikasjon,
        visningsnavn = "${lovKilde.beskrivelse} $spesifikasjon",
    )
}

