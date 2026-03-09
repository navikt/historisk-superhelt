package no.nav.sakstatistikk
import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.annotation.JsonNaming
import java.time.Instant
import java.time.LocalDate

/**
 * Representerer en melding til felles saksbehandlingsstatistikk i NAV.
 * Alle fagsystemer/vedtaksløsninger skal sende disse informasjonselementene
 * gjennom avtalt grensesnitt.
 *
 * Generert med copilot fra https://confluence.adeo.no/spaces/navdvh/pages/494772092/Teknisk+beskrivelse+av+behov+til+felles+saksbehandlingsstatistikk
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SaksbehandlingsStatistikk(

    /**
     * Fagsystemets behandlings-ID. Kan være lik [behandlingUuid].
     */
    val behandlingId: String,

    /**
     * Fagsystemets behandlings-UUID, hvis forskjellig fra [behandlingId].
     */
//    val behandlingUuid: String? = null,

    /**
     * Referanse til foregående behandling som denne oppstod med bakgrunn i.
     * Ved klage skal denne vise til den påklagede behandlingen.
     */
    val relatertBehandlingId: String? = null,

    /**
     * Fagsystemet til den relaterte behandlingen. Skal fylles ut når [relatertBehandlingId] er satt.
     */
    val relatertFagsystem: String? = null,

    /**
     * Saks-ID fra fagsystemet, jmf. Sak-begrepet i begrepskatalogen.
     */
    val sakId: String,

    /**
     * Saksnummer som brukes av saksbehandler for oppslag i vedtaksløsningen.
     */
    val saksnummer: String,

    /**
     * Aktør-ID tilknyttet søker eller hovedaktør for ytelsen.
     * Alternativt kan fødselsnummer benyttes..
     */
    val aktorId: String,

    /**
     * Tidspunkt da behandlingen oppstod (f.eks. søknad mottas). Starten på beregning av saksbehandlingstid.
     * Må være mindre eller lik [registrertTid]. Må være utfylt før behandlingen avsluttes.
     * Tidligere meldinger må re-sendes ved oppdatering av dette feltet.
     * Tidssone: UTC.
     */
    val mottattTid: Instant? = null,

    /**
     * Tidspunkt da behandlingen første gang ble registrert i fagsystemet.
     * Ved digitale søknader bør denne være tilnærmet lik [mottattTid].
     * Tidssone: UTC.
     */
    val registrertTid: Instant ?= null,

    /**
     * Tidspunkt når behandlingen ble avsluttet – enten avbrutt, henlagt, vedtak innvilget/avslått, osv.
     * Tidssone: UTC.
     */
    val ferdigBehandletTid: Instant? = null,

    /**
     * Dato for første utbetaling av ytelse.
     */
    val utbetaltTid: LocalDate? = null,

    /**
     * Tidspunkt for siste endring på behandlingen.
     * Ved første melding vil denne være lik [registrertTid].
     * Tidssone: UTC, inkl. millisekunder.
     */
    val endretTid: Instant,

    /**
     * Dato for når ytelsen normalt skal utbetales (planlagt uttak, ønsket oppstart e.l.).
     */
//    val forventetOppstartTid: LocalDate? = null,

    /**
     * Tidspunktet da fagsystemet legger hendelsen på grensesnittet/topicen.
     * Tidssone: UTC, inkl. millisekunder.
     */
    val tekniskTid: Instant = Instant.now(),

    /**
     * Kode som angir hvilken ytelse/stønad behandlingen gjelder.
     * Kan ha flere nivåer, f.eks. BARNETRYGD_ORD, BARNETRYGD_UTV.
     * Eks: SYKEPENGER, ALDERSPENSJON, BARNETRYGD.
     */
    val sakYtelse: Enum<*>,

    /**
     * Kode som angir om saken har et utenlandstilsnitt.
     */
    val sakUtland: SakUtland? = null,

    /**
     * Kode som angir hvilken type behandling det er snakk om.
     */
    val behandlingType: BehandlingType,

    /**
     * Kode som angir hvilken status behandlingen har.
     */
    val behandlingStatus: Enum<*>,

    /**
     * Kode som angir resultatet på behandlingen.
     * Må være utfylt hvis [behandlingStatus] er AVSLUTTET.
     * Ved klage: angi behandlingsresultat fra vedtaksinstans selv om det ikke er endelig.
     */
    val behandlingResultat: Enum<*>? = null,

    /**
     * Kode som angir begrunnelse til resultat.
     * Ved underkjennelse fra beslutter: f.eks. SAKSBEHANDLINGSFEIL, FEIL_FAKTUM.
     * Ved avslag: f.eks. VILKAR_XX_FEILET, DUBLETT, TEKNISK_AVVIK.
     * Ved innvilgelse: f.eks. ENDRET_FAKTUM, VILKAR_OPPFYLT, ENDRET_INNTEKT.
     * Ved klage og omgjøring: f.eks. FEIL_LOVANVENDELSE, ENDRET_FAKTUM, SAKSBEHANDLINGSFEIL.
     */
    val resultatBegrunnelse: Enum<*>? = null,

    /**
     * Kode som angir om hendelsen er manuell eller automatisk.
     * Akkumulert verdi: hvis behandlingen noen gang har hatt en manuell hendelse,
     * skal fremtidige rader ha verdien [BehandlingMetode.MANUELL].
     */
    val behandlingMetode: BehandlingMetode = BehandlingMetode.MANUELL,

    /**
     * Kode som angir årsak til opprettelse av behandling.
     * Eks. for SOKNAD: NYTT_BARN, NY_PERIODE.
     * Eks. for REVURDERING: ENDRET_INNTEKT, ENDRET_PERIODE.
     */
    val behandlingAarsak: Enum<*>? = null,

    /**
     * Nav-Ident til saksbehandler som opprettet behandlingen.
     * Hvis det er en servicebruker eller systemet selv, sendes info om denne.
     * Geo-lokaliserende: oppgis som "-5" hvis noen tilknyttede personer er kode 6.
     */
    val opprettetAv: String?= null,

    /**
     * Nav-Ident til saksbehandler som jobber med behandlingen.
     * Geo-lokaliserende: oppgis som "-5" hvis noen tilknyttede personer er kode 6.
     */
    val saksbehandler: String? = null,

    /**
     * Nav-Ident til ansvarlig beslutter ved krav om totrinnskontroll.
     * Geo-lokaliserende: oppgis som "-5" hvis noen tilknyttede personer er kode 6,
     * men kun om feltet ellers skulle hatt verdi.
     */
    val ansvarligBeslutter: String? = null,

    /**
     * Organisasjons-ID til NAV-enhet som har ansvar for behandlingen.
     * Ved nasjonal kø benyttes køens Org-ID.
     * Ved klage og oversendelse til KA-instans forventes en 42-enhet.
     * Geo-lokaliserende: oppgis som "-5" hvis noen tilknyttede personer er kode 6.
     */
    val ansvarligEnhet: String,

    /**
     * Beløp til innkreving. Gjelder kun behandlingstype [BehandlingType.TILBAKEKREVING].
     */
    val tilbakekrevBeloep: Double? = null,

    /**
     * Start på perioden feilutbetalingen gjelder. Gjelder kun [BehandlingType.TILBAKEKREVING].
     */
    val funksjonellPeriodeFom: LocalDate? = null,

    /**
     * Slutten av perioden feilutbetalingen gjelder. Gjelder kun [BehandlingType.TILBAKEKREVING].
     */
    val funksjonellPeriodeTom: LocalDate? = null,

    /**
     * Fagsystemets eget navn.
     */
    val fagsystemNavn: String,

    /**
     * Versjon av koden dataene er generert med bakgrunn i. Kan relateres til Git-repo.
     */
    val fagsystemVersjon: String,
)

/**
 * Angir om saken har et utenlandstilsnitt.
 * Se utenlandssak i begrepskatalogen.
 */
enum class SakUtland {
    /** Saken er nasjonal uten utenlandstilknytning. */
    NASJONAL,

    /** Saken har utenlandstilsnitt. */
    UTLAND,

    /** Saken er tilknyttet EØS-regelverket. */
    EOS, // EØS
}


/**
 * Angir om behandlingshendelsen er manuell eller automatisk.
 */
enum class BehandlingMetode {
    /** Behandlingen er utført manuelt av saksbehandler. */
    MANUELL,

    /** Behandlingen er utført automatisk av systemet. */
    AUTOMATISK,
}

enum class BehandlingType {
    SØKNAD,
    REVURDERING,
    KLAGE,
}
