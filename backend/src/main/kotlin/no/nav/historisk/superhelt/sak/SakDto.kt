package no.nav.historisk.superhelt.sak

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.infrastruktur.getCurrentNavIdent
import no.nav.historisk.superhelt.person.MaskertPersonIdent
import no.nav.historisk.superhelt.person.toMaskertPersonIdent
import no.nav.historisk.superhelt.sak.model.*
import no.nav.person.Fnr
import java.time.LocalDate

data class SakDto(
    val saksnummer: Saksnummer,
    val type: SaksType,
    val fnr: Fnr,
    val maskertPersonIdent: MaskertPersonIdent,
    val tittel: String?,
    val begrunnelse: String?,
    val status: SakStatus,
    val opprettetDato: LocalDate,
    val saksbehandler: String,
)

data class SakCreateRequestDto(
    val type: SaksType,
    @field:Size(min = 11, max = 11)
    @field:Pattern(regexp = "[0-9]*", message = "Fødselsnummer må kun inneholde tall")
    val fnr: Fnr,
    val tittel: String? = null,
    val begrunnelse: String? = null
)

data class SakUpdateRequestDto(
    val tittel: String? = null,
    val begrunnelse: String? = null,
    val status: SakStatus? = null,
    val vedtak: VedtakType? = null
)

// Extension functions for mapping between Entity and DTO
fun SakEntity.toResponseDto(): SakDto {
    return SakDto(
        saksnummer = this.saksnummer,
        type = this.type,
        fnr = this.fnr,
        maskertPersonIdent = this.fnr.toMaskertPersonIdent(),
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        opprettetDato = this.opprettet.toLocalDate(),
        saksbehandler = this.saksBehandler,
    )
}

// Todo flytte inn i service
fun SakCreateRequestDto.toEntity(): SakEntity {
    return SakEntity(
        type = this.type,
        fnr = this.fnr,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = SakStatus.UNDER_BEHANDLING,
        saksBehandler = getCurrentNavIdent() ?: "ukjent"
    )
}

fun SakUpdateRequestDto.updateEntity(entity: SakEntity): SakEntity {
    this.tittel?.let { entity.tittel = it }
    this.begrunnelse?.let { entity.begrunnelse = it }
    this.status?.let { entity.status = it }
    return entity
}

