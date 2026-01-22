package no.nav.historisk.superhelt.dokarkiv.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.sak.StonadsType

data class JournalforRequest(
    @field:NotNull
    val stonadsType: StonadsType,
    @field:NotNull
    val jfrOppgaveId: EksternOppgaveId,
    @field:NotNull
    val bruker: FolkeregisterIdent,
    // TODO Avsender kan også være virksomhet
    @field:NotNull
    val avsender: FolkeregisterIdent,
    @field:NotEmpty
    @field:Valid
    val dokumenter: List<JournalforDokument>,
) {
    data class JournalforDokument(
        @field:NotNull
        val tittel: String,
        @field:NotNull
        val dokumentInfoId: EksternDokumentInfoId,
        val logiskeVedlegg: List<String>? = emptyList(),
    )
}