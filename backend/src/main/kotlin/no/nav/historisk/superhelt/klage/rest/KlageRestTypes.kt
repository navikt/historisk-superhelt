package no.nav.historisk.superhelt.klage.rest

import jakarta.validation.constraints.Size
import java.time.LocalDate

data class SendKlageRequestDto(
    @field:Size(min = 1, max = 100)
    val hjemmelId: String,
    val datoKlageMottatt: LocalDate,
    @field:Size(max = 2000)
    val kommentar: String? = null,
)

data class HjemmelDto(
    val id: String,
    val lovKildeNavn: String,
    val lovKildeBeskrivelse: String,
    val spesifikasjon: String,
    /** Kortform visningsnavn, f.eks. "Ftrl § 10-3" */
    val visningsnavn: String,
)

