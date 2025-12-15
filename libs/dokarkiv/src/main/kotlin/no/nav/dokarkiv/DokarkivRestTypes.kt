package no.nav.dokarkiv

import com.fasterxml.jackson.annotation.JsonProperty

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
   @JsonProperty("dokumenter") val dokumenter: List<DokumentInfo>,
   @JsonProperty("journalpostId") val journalpostId: EksternJournalpostId,
   @JsonProperty("journalpostferdigstilt") val journalpostferdigstilt: Boolean,
   @JsonProperty("journalstatus") val journalstatus: String? = null,
   @JsonProperty("melding") val melding: String? = null,
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
