package no.nav.kabal.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Request-modell for oversendelse av klage/anke til Kabal API v4.
 *
 * Felt påkrevd av Kabal API (non-nullable her):
 * type, sakenGjelder, fagsak, kildeReferanse, hjemler,
 * forrigeBehandlendeEnhet, tilknyttedeJournalposter, ytelse
 */
data class SendSakV4Request(
    /** KLAGE eller ANKE */
    val type: SakType,
    /** Person saken gjelder */
    val sakenGjelder: SakenGjelder,
    /** Klager – som regel samme som sakenGjelder */
    val klager: Klager,
    val prosessfullmektig: Prosessfullmektig? = null,
    /** Fagsak-referanse fra kildesystemet */
    val fagsak: Fagsak,
    /** Teknisk ID i avsendersystemet – brukes av Kabal ved tilbakemelding */
    val kildeReferanse: String,
    /** Id som rapporteres til DVH. Kabal bruker kildeReferanse hvis denne ikke er satt. */
    val dvhReferanse: String? = null,
    /** Hjemler som liste av streng-ID-er (f.eks. "FTRL_10_7I"). Send minst én. */
    val hjemler: List<String> = emptyList(),
    /** NAV-enhet til saksbehandler som oppretter klagen – hentes fra EntraProxy */
    val forrigeBehandlendeEnhet: String,
    /** Kan sendes som tom liste, eller med klage/andre journalposter */
    val tilknyttedeJournalposter: List<TilknyttetJournalpost> = emptyList(),
    /** Dato klagen ble mottatt – fra Joark eller saksbehandler fritekst, ikke start i Superhelt */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val brukersKlageMottattVedtaksinstans: LocalDate? = null,
    /** Brukes kun når Kabal er nede */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val frist: LocalDate? = null,
    /** Tidspunkt saken ble mottatt i KA – ikke påkrevd, settes til starttidspunkt i Superhelt */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    val sakMottattKaTidspunkt: LocalDateTime? = null,
    /** Ytelse-kode fra Kabal-kodeverk (f.eks. "HEL_HEL" for Hjelpemidler – Ortopediske). Påkrevd av Kabal API. */
    val ytelse: String,
    val kommentar: String? = null,
    /** Ikke sett – gjelder svarbrev/oversendelsesbrev fra Kabal */
    val hindreAutomatiskSvarbrev: Boolean = false,
    /** Ikke i bruk – ble brukt ved migrering */
    val saksbehandlerIdentForTildeling: String? = null,
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
    val adresselinje1: String,
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
