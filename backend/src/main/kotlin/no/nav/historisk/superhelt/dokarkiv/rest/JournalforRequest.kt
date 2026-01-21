package no.nav.historisk.superhelt.dokarkiv.rest

import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.dokarkiv.EksternDokumentInfoId
import no.nav.historisk.superhelt.sak.StonadsType

data class JournalforRequest(
    val behandlingstype: StonadsType,
    val jfrOppgave: EksternOppgaveId,
    val bruker: FolkeregisterIdent,
    // TODO Avsender kan også være virksomhet
    val avsender: FolkeregisterIdent,
    val dokumenter: List<JournalforDokument>,
) {
    data class JournalforDokument(
        val tittel: String,
        val dokumentInfoId: EksternDokumentInfoId,
        val logiskeVedlegg: List<String>? = emptyList(),
    )
}