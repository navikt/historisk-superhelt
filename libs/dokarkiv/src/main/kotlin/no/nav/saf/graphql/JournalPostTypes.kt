package no.nav.saf.graphql

import no.nav.common.types.EksternJournalpostId
import no.nav.dokarkiv.AvsenderMottakerIdType
import no.nav.dokarkiv.BrukerIdType
import no.nav.dokarkiv.EksternDokumentInfoId

data class Journalpost(
    val journalpostId: EksternJournalpostId,
    val journalstatus: JournalStatus,
    val tittel: String? = null,
    val sak: JournalpostSak? = null,
    val bruker: JournalpostBruker? = null,
    val avsenderMottaker: JournalpostAvsenderMottaker? = null,
    val dokumenter: List<JournalpostDokumentInfo>? = emptyList(),
)

data class JournalpostSak(
    val fagsaksystem: String? = null,
    val fagsakId: String? = null,
)

data class JournalpostDokumentInfo(
    val tittel: String? = null,
    val dokumentInfoId: EksternDokumentInfoId,
    val dokumentvarianter: List<JournalpostDokumentVariant>? = emptyList(),
)

data class JournalpostDokumentVariant(
    val filtype: String? = null,
    val filnavn: String? = null,
    val saksbehandlerHarTilgang: Boolean,
)

data class JournalpostBruker(
    val id: String? = null,
    val type: BrukerIdType? = null,
)

data class JournalpostAvsenderMottaker(
    val id: String? = null,
    val type: AvsenderMottakerIdType? = null,
    val navn: String? = null,
)

enum class JournalStatus {
    /**
     * Journalposten er mottatt, men ikke journalført. "Mottatt" er et annet ord for "arkivert" eller "midlertidig journalført".
     * Statusen vil kun forekomme for inngående dokumenter.
     */
    MOTTATT,

    /**
     * Journalposten er ferdigstilt og ansvaret for videre behandling av forsendelsen er overført til fagsystemet.
     * Journalposter med status JOURNALFOERT oppfyller minimumskrav til metadata i arkivet, som for eksempel tema, sak, bruker og avsender.
     */
    JOURNALFOERT,

    /**
     * Journalposten med tilhørende dokumenter er ferdigstilt, og journalen er i prinsippet låst for videre endringer.
     * Tilsvarer statusen JOURNALFOERT for inngående dokumenter.
     */
    FERDIGSTILT,

    /**
     * Dokumentet er sendt til bruker. Statusen benyttes også når dokumentet er tilgjengeliggjort for bruker på Nav.no, og bruker er varslet.
     * Statusen kan forekomme for utgående dokumenter og notater.
     */
    EKSPEDERT,

    /**
     * Journalposten er opprettet i arkivet, men fremdeles under arbeid.
     * Statusen kan forekomme for utgående dokumenter og notater.
     */
    UNDER_ARBEID,

    /**
     * Journalposten har blitt unntatt fra saksbehandling etter at den feilaktig har blitt knyttet til en sak. Det er ikke mulig å slette en saksrelasjon, istedet settes saksrelasjonen til feilregistrert.
     * Statusen kan forekomme for alle journalposttyper.
     */
    FEILREGISTRERT,

    /**
     * Journalposten er unntatt fra saksbehandling. Status UTGAAR brukes stort sett ved feilsituasjoner knyttet til skanning eller journalføring.
     * Statusen vil kun forekomme for inngående dokumenter.
     */
    UTGAAR,

    /**
     * Utgående dokumenter og notater kan avbrytes mens de er under arbeid, og ikke enda er ferdigstilt. Statusen AVBRUTT brukes stort sett ved feilsituasjoner knyttet til vedtaksproduksjon.
     * Statusen kan forekomme for utgående dokumenter og notater.
     */
    AVBRUTT,

    /**
     * Journalposten har ikke noen kjent bruker.
     * NB: UKJENT_BRUKER er ikke en midlertidig status, men benyttes der det ikke er mulig å journalføre fordi man ikke klarer å identifisere brukeren forsendelsen gjelder.
     * Statusen kan kun forekomme for inngående dokumenter.
     */
    UKJENT_BRUKER,

    /**
     * Statusen benyttes bl.a. i forbindelse med brevproduksjon for å reservere 'plass' i journalen for dokumenter som skal populeres på et senere tidspunkt.
     * Tilsvarer statusen OPPLASTING_DOKUMENT for inngående dokumenter.
     * Statusen kan forekomme for utgående dokumenter og notater.
     */
    RESERVERT,

    /**
     * Midlertidig status på vei mot MOTTATT.
     * Dersom en journalpost blir stående i status OPPLASTING_DOKUMENT over tid, tyder dette på at noe har gått feil under opplasting av vedlegg ved arkivering.
     * Statusen kan kun forekomme for inngående dokumenter.
     */
    OPPLASTING_DOKUMENT,

    /**
     * Dersom statusfeltet i Joark er tomt, mappes dette til UKJENT.
     */
    UKJENT,
}
