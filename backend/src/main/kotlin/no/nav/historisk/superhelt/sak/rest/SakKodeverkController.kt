package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.klage.rest.HjemmelDto
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.historisk.superhelt.vedtak.VedtaksResultat
import no.nav.kabal.model.Hjemmel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak/kodeverk")
class SakKodeverkController {

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

    @Operation(operationId = "getKodeverkStonadType",)
    @GetMapping("stonadtyper")
    fun stonadstypeKodeverk(): List<StonadsTypeDto> {
       return StonadsType.entries.map { StonadsTypeDto(it) }
    }

    @Operation(operationId = "getKodeverkSakStatus")
    @GetMapping("sakstatuser")
    fun sakStatusKodeverk(): List<SakStatusKodeDto> {
        return SakStatus.entries.map { SakStatusKodeDto(it) }
    }

    @Operation(operationId = "getKodeverkVedtaksResultat")
    @GetMapping("vedtaksresultater")
    fun vedtaksResultatKodeverk(): List<VedtaksResultatDto> {
        return VedtaksResultat.entries.map { VedtaksResultatDto(it) }
    }

    data class StonadsTypeDto(val type: StonadsType) {
        val navn: String = type.navn
        val beskrivelse: String? = type.beskrivelse
    }

    data class SakStatusKodeDto(val status: SakStatus) {
        val navn: String = status.navn
    }

    data class VedtaksResultatDto(val vedtaksResultat: VedtaksResultat) {
        val navn: String = vedtaksResultat.navn
    }

}
