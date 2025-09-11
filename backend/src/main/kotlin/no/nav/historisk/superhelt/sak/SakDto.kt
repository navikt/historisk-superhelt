package no.nav.historisk.superhelt.sak

import jakarta.validation.constraints.Size
import no.nav.historisk.superhelt.sak.model.*

data class SakDto(
    val saksnummer: Saksnummer,
    val type: StonadsType,
    val person: String,
    val tittel: String?,
    val begrunnelse: String?,
    val status: SakStatus,
    val vedtak: VedtakType?
)

data class SakCreateRequestDto(
    val type: StonadsType,
    @Size(min = 11, max = 11)
    val person: Personident,
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
        person = this.person,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse,
        status = this.status,
        vedtak = this.vedtak
    )
}

fun SakCreateRequestDto.toEntity(): SakEntity {
    return SakEntity(
        type = this.type,
        person = this.person,
        tittel = this.tittel,
        begrunnelse = this.begrunnelse
    )
}

fun SakUpdateRequestDto.updateEntity(entity: SakEntity): SakEntity {
    this.tittel?.let { entity.tittel = it }
    this.begrunnelse?.let { entity.begrunnelse = it }
    this.status?.let { entity.status = it }
    this.vedtak?.let { entity.vedtak = it }
    return entity
}

