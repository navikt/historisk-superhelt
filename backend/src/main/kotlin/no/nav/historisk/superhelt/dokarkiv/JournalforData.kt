package no.nav.historisk.superhelt.dokarkiv

import jakarta.validation.constraints.NotNull
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.dokarkiv.EksternDokumentInfoId

interface JournalforData {
    val jfrOppgaveId: EksternOppgaveId
    val bruker: FolkeregisterIdent
    val avsender: FolkeregisterIdent
    val dokumenter: List<JournalforDokument>

    fun getTittel(): String {
        return dokumenter.firstOrNull()?.tittel ?: "Ukjent tittel"
    }
}

data class JournalforDokument(
    @field:NotNull
    val tittel: String,
    @field:NotNull
    val dokumentInfoId: EksternDokumentInfoId,
    val logiskeVedlegg: List<String>? = emptyList(),
)