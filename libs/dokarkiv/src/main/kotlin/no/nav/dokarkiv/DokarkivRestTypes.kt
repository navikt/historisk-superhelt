package no.nav.dokarkiv

data class JournalpostRequest(
    val tittel: String,
    val journalpostType: JournalpostType,
    val tema: EksternFellesKodeverkTema,
    val avsenderMottaker: AvsenderMottaker?,
    val eksternReferanseId: String?,
    val dokumenter: List<Dokument>,
    val bruker: Bruker,
    val kanal: Kanal?,
    val sak: Sak,
    val journalfoerendeEnhet: Enhetsnummer?,
)

data class JournalpostResponse(
    val dokumenter: List<DokumentInfo>,
    val journalpostId: EksternJournalpostId,
    val journalpostferdigstilt: Boolean,
    val journalstatus: String? = null,
    val melding: String? = null,
)

data class OppdaterJournalpostRequest(
    val sak: Sak,
    val tittel: String,
    val bruker: Bruker,
    val avsenderMottaker: AvsenderMottaker,
    val tema: EksternFellesKodeverkTema,
    val dokumenter: List<DokumentMedTittel>?,
)

data class DokumentMedTittel(
    val tittel: String,
    val dokumentInfoId: EksternDokumentInfoId,
)
