package no.nav.oppgave.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

data class PatchOppgaveRequest(
    /** Nåværende versjon på oppgaven */
    val versjon: Int,
    /** Organisasjonsnummer. Kan ikke nullstilles, men kan endres til personident. Merk at det kun er i helt spesielle tilfeller ident skal endres, i.e ifm journalføring. Kun én av personident eller orgnr kan angis */
    val orgnr: String? = null,
    /** Hvilken status oppgaven har. Konsumenter bør kun forholde seg til dette ved behov for å skille mellom ferdigstilt og feilregistrert */
    val status: Status? = null,
    /** Enhet (eller virtuell enhet, ref. norg2) medarbeider representerer når endringen utføres. Skal alltid angis når utført av en medarbeider (OBO). For maskinelle prosesser skal verdien utelates (dersom en verdi angis, så vil systemet uansett overstyre til null) */
    val endretAvEnhetsnr: String? = null,
    /** Beskrivelse av oppgaven. Dette feltet skal som hovedregel aldri benyttes lenger ved patching av oppgaver. Endringer av fordeling, kategorier etc populeres automatisk i strukturert endringslogg. Kommentarer kan sendes inn i eget strukturert felt, uten spesifikasjon av hvem som kommenterte, når etc (lagres strukturert ut i fra tidspunkt og token).Systemet vil inntil videre automatisk populere beskrivelseshistorikken med bå de kommentarer og endringslogg  (endringslogg i beskrivelseshistorikken vil imidlertid kun inneholde informasjon om fordeling av oppgave, og endring av tema og oppgavetype). Dersom det er essensielt at beskrivelseshistorikken avviker fra det automatisk genererte, må denne fortsatt sendes med, da automatikk ved oppdatering av beskrivelse ikke gjennomføres når feltet er med i requesten med eksisternde eller oppdatert data */
    val beskrivelse: String? = null,
    /** Navident for ressursen som skal tildeles oppgaven */
    val tilordnetRessurs: String? = null,
    /** Enheten oppgaven skal tildeles */
    val tildeltEnhetsnr: String? = null,
    /** Angir hvilken prioritet oppgaven har */
    val prioritet: Prioritet? = null,
    /** Kategorisering av oppgaven. Må være tillatt for det aktuelle temaet og i kombinasjon med en ev. behandlingstype. Se api for kodeverk */
    val behandlingstema: String? = null,
    /** Kategorisering av oppgaven. Må være tillatt for det aktuelle temaet og i kombinasjon med et ev. behandlingstema. Se api for kodeverk */
    val behandlingstype: String? = null,
    /** Oppgavens frist for ferdigstillelse. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fristFerdigstillelse: LocalDate? = null,
    /** Benyttes for å legge en oppgave "på vent". Styrer visning i arbeidsflater, der oppgaver med dato <= dagens dato vises. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate? = null,
    /** Mer presis kategorisering av oppgaven. Må være tillatt for det aktuelle temaet. Se api for kodeverk */
    val oppgavetype: String? = null,
    /** Tema (fagområde) for oppgaven */
    val tema: String? = null,
    /** Angis for å knytte oppgaven til en journalpost i arkivet */
    val journalpostId: String? = null,
    /** Angis for å knytte oppgaven til en sak i et fagsystem */
    val saksreferanse: String? = null,
    /** Angis for å indikere hvilken applikasjon oppgaven skal behandles i */
    val behandlesAvApplikasjon: String? = null,
    /** Kommentar i tilknytning til endringer. Hensyntas kun ved patch */
    val kommentar: Kommentar? = null,
    /** ident for person oppgaven gjelder, dvs. fnr, dnr, npid eller aktørid. Kan ikke nullstilles. Kun én av personident eller orgnr kan angis. Merk at det kun er i helt spesielle tilfeller ident skal endres, i.e ifm journalføring */
    val personident: String? = null
)

data class Kommentar(
    /** Tekstinnholdet i kommentaren */
    val tekst: String,
    /** Skal settes til true dersom kommentaren er automatisk generert av et system, og det benyttes OBO-token. Ignoreres ved CCF */
    val automatiskGenerert: Boolean? = null
)