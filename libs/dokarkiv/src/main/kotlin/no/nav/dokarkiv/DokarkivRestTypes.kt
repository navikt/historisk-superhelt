package no.nav.dokarkiv

data class JournalpostRequest(
    val tittel: String,
    val journalpostType: JournalpostType,
    val tema: EksternFellesKodeverkTema,
    val avsenderMottaker: AvsenderMottaker?,
    val eksternReferanseId: String?,
    val dokumenter: List<Dokument>,
    val bruker: DokarkivBruker,
    val kanal: Kanal?,
    val sak: DokArkivSak,
    val journalfoerendeEnhet: Enhetsnummer?,
)

data class JournalpostResponse(
    val dokumenter: List<DokumentInfo>,
    val journalpostId: EksternJournalpostId,
    val journalpostferdigstilt: Boolean,
)

data class OppdaterJournalpostRequest(
    val sak: DokArkivSak,
    val tittel: String,
    val bruker: DokarkivBruker,
    val avsenderMottaker: AvsenderMottaker,
    val tema: EksternFellesKodeverkTema,
    val dokumenter: List<DokumentMedTittel>?,
)

data class DokumentMedTittel(
    val tittel: String,
    val dokumentInfoId: EksternDokumentInfoId,
)
