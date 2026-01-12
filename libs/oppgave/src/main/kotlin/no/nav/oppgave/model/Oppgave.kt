package no.nav.oppgave.model

import no.nav.common.types.AktorId
import no.nav.common.types.EksternJournalpostId
import no.nav.common.types.Enhetsnummer
import no.nav.common.types.NavIdent
import java.time.LocalDate
import java.time.OffsetDateTime

data class Oppgave(
    /** Syntetisk id */
    val id: Long,
    /** Enheten oppgaven er tildelt */
    val tildeltEnhetsnr: Enhetsnummer,
    /** Kategorisering av oppgaven. Hvilket tema/fagområde oppgaven tilhører */
    val tema: String,
    /** Kategorisering av hva slags oppgavetype det er. Hvilke oppgavetyper som er tillatt for et gitt tema er definert i oppgavekodeverket. */
    val oppgavetype: String,
    /** Brukes for å håndtere optimistisk låsing, hvor to brukere har skrevet på samme oppgave. Hver gang det gjøres til på en oppgave, økes verdien av VERSJON med 1. Når en klient skal lagre til på en oppgave, sendes verdien av VERSJON oppgaven hadde da informasjon om oppgaven som nå er endret ble hentet ut. Dersom verdien av VERSJON er endret, har noen andre lagret oppgaven i mellomtiden, og oppgaven kan ikke lagres. 409 Conflict vil returneres fra tjenesten. Under oppretting av oppgave trenger man ikke å spesifisere noen verdi for dette feltet, oppgaven vil starte på versjon 1 */
    val versjon: Int,
    /** Angir hvilken prioritet oppgaven har */
    val prioritet: Prioritet,
    /** Hvilken status oppgaven har. Konsumenter bør kun forholde seg til dette ved behov for å skille mellom ferdigstilt og feilregistrert */
    val status: Status,
    /** Angir når det er relevant å ta tak i oppgaven. Settes normalt til samme dato som oppgaven opprettes, men endres fremover i tid dersom oppgaven f.eks settes på vent. I Gosys vil oppgaver på vent som standard skjules, men kan hente frem om ønskelig */
    val aktivDato: LocalDate,
    /** ident for person, dvs. fnr, dnr, npid eller aktørid */
    val personident: String? = null,
    /** Enheten som endret oppgaven */
    val endretAvEnhetsnr: String? = null,
    /** Hvilken enhet som har opprettet oppgaven */
    val opprettetAvEnhetsnr: String? = null,
    /** Id for en journalpostreferanse */
    val journalpostId: EksternJournalpostId? = null,
    /** Hvilken applikasjon oppgaven skal behandles i */
    val behandlesAvApplikasjon: String? = null,
    /** Referanse til sak i fagsystem */
    val saksreferanse: String? = null,
    /** Syntetisk id for en person, kan hentes fra PDL */
    val aktoerId: AktorId? = null,
    /** Organisasjonsnummer. Bedriften oppgaven skal løses på vegne av */
    val orgnr: String? = null,
    /** NavIdent for ressursen som er tilordnet oppgaven. */
    val tilordnetRessurs: NavIdent? = null,
    /** MERK! Vi arbeider med å migrere bort fra ett stort tekstfelt til strukturerte kommentarer + strukturert endringslogg. Bruken av feltet er under utfasing i Gosys. Dersom ditt system benytter dette feltet ber vi dere følge disse retningslinjene\n\n: 1. Beskrivelse som registreres samtidig som oppgaven blir opprettet, skal _ikke_ ha header\n2. Når beskrivelsen benyttes til status-oppdateringer, kommentarer etc i etterkant så skal header på følgende format benyttes, og legges til foran den eksisterende beskrivelsen:  --- dd.MM.yyyy <navn på ansatt> (<navident/systemnavn>, <enhet endringen utføres på vegne av>)\\n\\n\n3. Ta i bruk kommentar-feltet for å registrere kommentarer fra saksbehandlere og/eller automatiske oppdateringer i tillegg til beskrivelsen, samt gi oss beskjed om at dette er gjort. Kommentaren skal _ikke_ ha headerLegacyfelt som benyttes til mye forskjellig. For å ivareta bakoverkompatibilitet, må konsumenter som ønsker at beskrivelseshistorikken skal være synlig i andre skjermbilder enn så lenge fortsette å benytte dette */
    val beskrivelse: String? = null,
    /** Kategoriserer oppgaven innenfor angitt tema. Som hovedregel skal det alltid angis enten behandlingstema, behandlingstype eller begge. Tillatte verdier er begrenset per tema, og endringer avklares med fagansvarlig/styringsenhet for området. Se /api/v1/kodeverk/gjelder/{tema} */
    val behandlingstema: String? = null,
    /** Kategoriserer oppgaven innenfor angitt tema. Som hovedregel skal det alltid angis enten behandlingstema, behandlingstype eller begge. Tillatte verdier er begrenset per tema, og endringer avklares med fagansvarlig/styringsenhet for området. Se /api/v1/kodeverk/gjelder/{tema} */
    val behandlingstype: String? = null,
    /** Hvilken mappe oppgaven er plassert i. Mapper administreres av den enkelte enhet. Mapper tilknyttet enheten hentes via eget endepunkt */
    val mappeId: Long? = null,
    /** Hvilken bruker eller system som opprettet oppgaven */
    val opprettetAv: String? = null,
    /** Hvilken bruker eller system som endret oppgaven sist */
    val endretAv: String? = null,
    /** Oppgavens frist for ferdigstillelse. */
    val fristFerdigstillelse: LocalDate? = null,
    /** Opprettet tidspunkt iht. ISO-8601 */
    val opprettetTidspunkt: OffsetDateTime? = null,
    /** Tidspunktet oppgaven ble ferdigstilt iht. ISO-8601 */
    val ferdigstiltTidspunkt: OffsetDateTime? = null,
    /** Tidspunktet oppgaven sist ble endret iht. ISO-8601 */
    val endretTidspunkt: OffsetDateTime? = null,
    /** Bruker oppgaven er tilknyttet */
    val bruker: Bruker? = null) {
    /** Bruker oppgaven er tilknyttet */
    data class Bruker(
        /** ident for person, dvs. fnr, dnr, npid eller aktørid */
        val ident: String,
        /** Bruker type */
        val type: BrukerType
    ){
        /** Bruker oppgaven er tilknyttet */
        enum class BrukerType {
            PERSON, ARBEIDSGIVER, SAMHANDLER
        }
    }



    /** Hvilken status oppgaven har. Konsumenter bør kun forholde seg til dette ved behov for å skille mellom ferdigstilt og feilregistrert */
    enum class Status {
        OPPRETTET,
        AAPNET,
        UNDER_BEHANDLING,
        FERDIGSTILT,
        FEILREGISTRERT
    }

    /** Angir hvilken prioritet oppgaven har */
    enum class Prioritet {
        HOY,
        NORM,
        LAV
    }


}

