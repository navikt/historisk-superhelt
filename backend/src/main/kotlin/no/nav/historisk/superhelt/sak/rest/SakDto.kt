package no.nav.historisk.superhelt.sak.rest

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.Sak
import no.nav.historisk.superhelt.sak.SakStatus
import no.nav.historisk.superhelt.sak.Saksnummer
import no.nav.historisk.superhelt.sak.StonadsType
import no.nav.person.Fnr
import java.time.LocalDate

data class SakDto(
    val saksnummer: Saksnummer,
    val type: StonadsType,
    val fnr: Fnr,
    val maskertPersonIdent: MaskertPersonIdent,
    val tittel: String?,
    val begrunnelse: String?,
    val status: SakStatus,
    val opprettetDato: LocalDate,
    val soknadsDato: LocalDate?,
    val saksbehandler: String,
)

data class SakCreateRequestDto(
    val type: StonadsType,
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "[0-9]*", message = "Fødselsnummer må kun inneholde tall")
    val fnr: Fnr,
    val tittel: String? = null,
    val soknadsDato: LocalDate? = null,
)

data class SakUpdateRequestDto(
    val type: StonadsType? = null,
    val tittel: String? = null,
    val begrunnelse: String? = null,
    val soknadsDato: LocalDate? = null,
    val status: SakStatus? = null,
)

// Extension functions for mapping between Domain and DTO
fun Sak.toResponseDto(): SakDto {
    return SakDto(
        saksnummer = this.saksnummer ?: Saksnummer("Ukjent"),
        type = this.type,
        fnr = this.fnr,
        maskertPersonIdent = this.fnr.toMaskertPersonIdent(),
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        opprettetDato = this.opprettetDato,
        saksbehandler = this.saksbehandler,
        soknadsDato = this.soknadsDato
    )
}

