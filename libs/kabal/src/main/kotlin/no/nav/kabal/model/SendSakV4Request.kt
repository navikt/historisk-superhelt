package no.nav.kabal.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class SendSakV4Request(
    val type: SakType,
    val sakenGjelder: SakenGjelder,
    val klager: Klager,
    val prosessfullmektig: Prosessfullmektig? = null,
    val fagsak: Fagsak,
    val kildeReferanse: String? = null,
    val dvhReferanse: String? = null,
    val hjemler: List<Hjemmel> = emptyList(),
    val forrigeBehandlendeEnhet: String? = null,
    val tilknyttedeJournalposter: List<TilknyttetJournalpost> = emptyList(),
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val brukersKlageMottattVedtaksinstans: LocalDate? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val frist: LocalDate? = null,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val sakMottattKaTidspunkt: LocalDateTime? = null,
    val ytelse: String? = null,
    val kommentar: String? = null,
    val hindreAutomatiskSvarbrev: Boolean = false,
    val saksbehandlerIdentForTildeling: String? = null
)

enum class SakType {
    KLAGE,
    ANKE
}

data class Ident(
    val type: IdentType,
    val verdi: String
)

enum class IdentType {
    PERSON,
    VIRKSOMHET
}

data class SakenGjelder(
    val id: Ident
)

data class Klager(
    val id: Ident
)

/**
 * Prosessfullmektig i saken. Brukes bl.a. til automatisk svarbrev.
 */
data class Prosessfullmektig(
    val id: Ident,
    val navn: String? = null,
    val adresse: Adresse? = null
)

data class Adresse(
    val adresselinje1: String? = null,
    val adresselinje2: String? = null,
    val adresselinje3: String? = null,
    val postnummer: String? = null,
    val poststed: String? = null,
    /** ISO 3166-1 alpha-2 kode. F.eks. NO for Norge. */
    val land: String? = null
)

data class Fagsak(
    val fagsakId: String,
    val fagsystem: String
)

data class TilknyttetJournalpost(
    val type: JournalpostType,
    val journalpostId: String
)

enum class JournalpostType {
    BRUKERS_SOEKNAD,
    OPPRINNELIG_VEDTAK,
    BRUKERS_KLAGE,
    BRUKERS_ANKE,
    BRUKERS_OMGJOERINGSKRAV,
    BRUKERS_BEGJAERING_OM_GJENOPPTAK,
    OVERSENDELSESBREV,
    KLAGE_VEDTAK,
    ANNET
}

