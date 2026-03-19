package no.nav.historisk.superhelt.dokarkiv.rest

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import no.nav.common.types.EksternOppgaveId
import no.nav.common.types.FolkeregisterIdent
import no.nav.common.types.Saksnummer
import no.nav.historisk.superhelt.dokarkiv.JournalforData
import no.nav.historisk.superhelt.dokarkiv.JournalforDokument
import no.nav.historisk.superhelt.sak.StonadsType

data class JournalforNySakRequest(
    @field:NotNull
    val stonadsType: StonadsType,
    @field:NotNull
    override val jfrOppgaveId: EksternOppgaveId,
    @field:NotNull
    override val bruker: FolkeregisterIdent,
    @field:NotNull
    override val avsender: FolkeregisterIdent,
    @field:NotEmpty
    @field:Valid
    override val dokumenter: List<JournalforDokument>,
) : JournalforData

data class JournalforEksisterendeSakRequest(
    @field:NotNull
    val saksnummer: Saksnummer,
    @field:NotNull
    override val jfrOppgaveId: EksternOppgaveId,
    @field:NotNull
    override val bruker: FolkeregisterIdent,
    @field:NotNull
    override val avsender: FolkeregisterIdent,
    @field:NotEmpty
    @field:Valid
    override val dokumenter: List<JournalforDokument>,
) : JournalforData

