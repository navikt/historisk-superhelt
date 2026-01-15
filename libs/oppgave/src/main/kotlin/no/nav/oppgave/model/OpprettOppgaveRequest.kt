package no.nav.oppgave.model

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.common.types.EksternJournalpostId
import java.time.LocalDate
import java.util.*

data class OpprettOppgaveRequest(
    /** ident for person oppgaven knyttes mot, dvs. fnr, dnr, npid eller aktørid, Kan kun knyttes mot én bruker */
    val personident: String? = null,
    /** Organisasjonsnummer oppgaven knyttes mot. Kan kun knyttes mot én bruker */
    val orgnr: String? = null,
    /** Samnhandlernummer. Ta kontakt med #team-oppgavehåndtering dersom dere ønsker å bruke samhandlernr, da dette skal fases ut */
    val samhandlernr: String? = null,
    /** Enheten oppgaven tildeles. Dersom enhet ikke er angitt, så vil oppgaven automatisk bli forsøkt fordeling iht. standard arbeidsfordelingsregler (se norg2) */
    val tildeltEnhetsnr: String? = null,
    /** Hvilken enhet som har opprettet oppgaven. Skal alltid angis dersom oppgaven registreres av en medarbeider */
    val opprettetAvEnhetsnr: String? = null,
    /** Id for en journalpostreferanse. Benyttes når oppgaven skal kobles mot journalposter, i.e for journalføringsoppgaver */
    val journalpostId: EksternJournalpostId? = null,
    /** Indikerer hvilken applikasjon oppgaven skal behandles i. Hvis angitt vil dette begrense hva man kan endre på av oppgaven i Gosys */
    val behandlesAvApplikasjon: String? = null,
    /** Knytter oppgaven til en sak i et fagsystem */
    val saksreferanse: String? = null,
    /** Kort tekstlig beskrivelse av oppgaven. Vennligst overhold følgende retningslinjer 1. Det skal ikke benyttes header (unngå alle varianter av --- dd.mm.yyyy (XXXXXX, <enhetsnr>) --- 2. Skal ikke være nødvendig informasjon for saksbehandlere til å finne riktig oppgave i en liste. Til dette skal tema, oppgavetype, behandlingstema, behandlingstype benyttes 3. Unngå personsensitiv og/eller lokaliserende informasjon, da dette feltet enn så lenge presenteres i en god del arbeidslister i eldre systemer */
    val beskrivelse: String? = null,
    /** Tema for oppgaven */
    val tema: String,
    /** Kategorisering av oppgaven. Som hovedregel skal alltid minst en av behandlingstema og/eller behandlingstype være angitt. Må være tillatt for det aktuelle temaet og i kombinasjon med en ev. behandlingstype. Se api for kodeverk */
    val behandlingstema: String? = null,
    /** Kategorisering av oppgaven. Må være tillatt for det aktuelle temaet. Se api for kodeverk */
    val oppgavetype: String,
    /** Kategorisering av oppgaven. Som hovedregel skal alltid minst en av behanlingstema og/eller behandlingstype være angitt. Må være tillatt for det aktuelle temaet og i kombinasjon med et ev. behandlingstema. Se api for kodeverk */
    val behandlingstype: String? = null,
    /** Benyttes for å legge en oppgave "på vent". Sette normalt til dagens dato for nye oppgaver. Styrer visning i arbeidsflater, der oppgaver med dato <= dagens dato vises. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val aktivDato: LocalDate= LocalDate.now(),
    /** Oppgavens frist for ferdigstillelse. */
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val fristFerdigstillelse: LocalDate? = null,
    /** Angir hvilken prioritet oppgaven har */
    val prioritet: Oppgave.Prioritet = Oppgave.Prioritet.NORM,
    /** Unik identifikator for duplikatkontroll */
    val uuid: UUID? = null
)