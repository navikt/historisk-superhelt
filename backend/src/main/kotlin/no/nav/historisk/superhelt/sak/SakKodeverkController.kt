package no.nav.historisk.superhelt.sak

import no.nav.historisk.superhelt.sak.model.SaksType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sak/kodeverk")
class SakKodeverkController {

    @GetMapping("typer")
    fun typeKodeverk(): List<SaksTypeDto> {
       return SaksType.entries.map { SaksTypeDto(it) }
    }


    data class SaksTypeDto(val type: SaksType) {
        val navn: String = type.navn
        val beskrivelse: String = type.beskrivelse
    }

}

