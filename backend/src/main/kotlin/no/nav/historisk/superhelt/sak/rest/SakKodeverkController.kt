package no.nav.historisk.superhelt.sak.rest

import io.swagger.v3.oas.annotations.Operation
import no.nav.historisk.superhelt.sak.StonadsType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak/kodeverk")
class SakKodeverkController {

    @Operation(operationId = "getKodeverkStonadType",)
    @GetMapping("stonadtyper")
    fun stonadstypeKodeverk(): List<StonadsTypeDto> {
       return StonadsType.entries.map { StonadsTypeDto(it) }
    }


    data class StonadsTypeDto(val type: StonadsType) {
        val navn: String = type.navn
        val beskrivelse: String? = type.beskrivelse
    }

}