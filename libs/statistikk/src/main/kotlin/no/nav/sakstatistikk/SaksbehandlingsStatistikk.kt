import com.fasterxml.jackson.annotation.JsonFormat
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
     * Løpenummer med identifiserende unik verdi for hver enkelt rad i BigQuery.
     * Gir DVH mulighet til å utvikle delta-last. Kun positive verdier.
     * Ikke nødvendig ved Kafka-utsending – kafka offset/timestamp dekker dette behovet.
     */
//    val sekvensnummer: Long? = null,

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
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val mottattTid: Instant? = null,

    /**
     * Tidspunkt da behandlingen første gang ble registrert i fagsystemet.
     * Ved digitale søknader bør denne være tilnærmet lik [mottattTid].
     * Tidssone: UTC.
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val registrertTid: Instant ?= null,

    /**
     * Tidspunkt når behandlingen ble avsluttet – enten avbrutt, henlagt, vedtak innvilget/avslått, osv.
     * Tidssone: UTC.
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val ferdigBehandletTid: Instant? = null,

    /**
     * Dato for første utbetaling av ytelse.
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val utbetaltTid: LocalDate? = null,

    /**
     * Tidspunkt for siste endring på behandlingen.
     * Ved første melding vil denne være lik [registrertTid].
     * Tidssone: UTC, inkl. millisekunder.
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val endretTid: Instant,

    /**
     * Dato for når ytelsen normalt skal utbetales (planlagt uttak, ønsket oppstart e.l.).
     */
//    val forventetOppstartTid: LocalDate? = null,

    /**
     * Tidspunktet da fagsystemet legger hendelsen på grensesnittet/topicen.
     * Tidssone: UTC, inkl. millisekunder.
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val tekniskTid: Instant = Instant.now(),

    /**
     * Kode som angir hvilken ytelse/stønad behandlingen gjelder.
     * Kan ha flere nivåer, f.eks. BARNETRYGD_ORD, BARNETRYGD_UTV.
     * Eks: SYKEPENGER, ALDERSPENSJON, BARNETRYGD.
     */
    val sakYtelse: String,

    /**
     * Kode som angir om saken har et utenlandstilsnitt.
     */
    val sakUtland: SakUtland? = null,

    /**
     * Kode som angir hvilken type behandling det er snakk om.
     */
    val behandlingType: String,

    /**
     * Kode som angir hvilken status behandlingen har.
     */
    val behandlingStatus: String,

    /**
     * Kode som angir resultatet på behandlingen.
     * Må være utfylt hvis [behandlingStatus] er [BehandlingStatus.AVSLUTTET].
     * Ved klage: angi behandlingsresultat fra vedtaksinstans selv om det ikke er endelig.
     */
    val behandlingResultat: String? = null,

    /**
     * Kode som angir begrunnelse til resultat.
     * Ved underkjennelse fra beslutter: f.eks. SAKSBEHANDLINGSFEIL, FEIL_FAKTUM.
     * Ved avslag: f.eks. VILKAR_XX_FEILET, DUBLETT, TEKNISK_AVVIK.
     * Ved innvilgelse: f.eks. ENDRET_FAKTUM, VILKAR_OPPFYLT, ENDRET_INNTEKT.
     * Ved klage og omgjøring: f.eks. FEIL_LOVANVENDELSE, ENDRET_FAKTUM, SAKSBEHANDLINGSFEIL.
     */
    val resultatBegrunnelse: String? = null,

    /**
     * Kode som angir om hendelsen er manuell eller automatisk.
     * Akkumulert verdi: hvis behandlingen noen gang har hatt en manuell hendelse,
     * skal fremtidige rader ha verdien [BehandlingMetode.MANUELL].
     */
    val behandlingMetode: BehandlingMetode,

    /**
     * Kode som angir årsak til opprettelse av behandling.
     * Eks. for SOKNAD: NYTT_BARN, NY_PERIODE.
     * Eks. for REVURDERING: ENDRET_INNTEKT, ENDRET_PERIODE.
     */
    val behandlingAarsak: String? = null,

    /**
     * Nav-Ident til saksbehandler som opprettet behandlingen.
     * Hvis det er en servicebruker eller systemet selv, sendes info om denne.
     * Geo-lokaliserende: oppgis som "-5" hvis noen tilknyttede personer er kode 6.
     */
    val opprettetAv: String,

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
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val funksjonellPeriodeFom: LocalDate? = null,

    /**
     * Slutten av perioden feilutbetalingen gjelder. Gjelder kun [BehandlingType.TILBAKEKREVING].
     */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
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

/**
 * Extension-funksjoner som lar konsumenter bruke egne enums i stedet for rå String-verdier.
 * Enum-verdiens `.name` brukes som statistikk-kode, slik at lib-en forblir String-basert.
 *
 * Eksempel:
 * ```kotlin
 * enum class BehandlingResultat { INNVILGET, AVSLATT }
 *
 * val statistikk = SaksbehandlingsStatistikk(...)
 *     .medBehandlingResultat(BehandlingResultat.INNVILGET)
 *     .medBehandlingStatus(MinBehandlingStatus.AVSLUTTET)
 * ```
 */
fun SaksbehandlingsStatistikk.medBehandlingResultat(resultat: Enum<*>?) =
    copy(behandlingResultat = resultat?.name)

fun SaksbehandlingsStatistikk.medResultatBegrunnelse(begrunnelse: Enum<*>?) =
    copy(resultatBegrunnelse = begrunnelse?.name)

fun SaksbehandlingsStatistikk.medBehandlingStatus(status: Enum<*>) =
    copy(behandlingStatus = status.name)

fun SaksbehandlingsStatistikk.medBehandlingType(type: Enum<*>) =
    copy(behandlingType = type.name)

fun SaksbehandlingsStatistikk.medBehandlingAarsak(aarsak: Enum<*>?) =
    copy(behandlingAarsak = aarsak?.name)