package no.nav.oppgave.model

data class FinnOppgaverParams(
    /** Statuskategori er en kategorisering av statuser internt i oppgave, dvs at det kan søkes på enten AAPEN eller AVSLUTTET og de relevante oppgave vil returneres uten at konsument trenger å spesifisere alle statuser som representerer åpne oppgaver eller motsatt (avsluttede oppgaver) */
    val statuskategori: String? = null,
    /** Filtrering på aktuelle tema (iht felles kodeverk) */
    val tema: List<String>? = null,
    /** Filtrering på aktuelle oppgavetyper (iht felles kodeverk) */
    val oppgavetype: List<String>? = null,
    /** Filtrering på tildelt enhet, enheten som har fått overført oppgaven til behandling */
    val tildeltEnhetsnr: String? = null,
    /** Hvilken ressurs (nav-ident) oppgaven er tilordnet */
    val tilordnetRessurs: String? = null,
    /** Filtrering på behandlingstema (iht felles kodeverk) */
    val behandlingstema: String? = null,
    /** Filtrering på behandlingstype (iht felles kodeverk) */
    val behandlingstype: String? = null,
    /** For å hente oppgaver knyttet til en gitt personbruker. 13-sifret aktørid (fnr,dnr,npid kan veksles via PDL) */
    val aktoerId: List<String>? = null,
    /** Angir journalpostId (fra arkivet/joark) oppgaven er knyttet til */
    val journalpostId: List<String>? = null,
    /** Søk etter oppgaver med angitt(e) saksreferanse(r) */
    val saksreferanse: List<String>? = null,
    /** Nedre grense for filtrering på ferdigstilt tidspunkt */
    val ferdigstiltFom: String? = null,
    /** Øvre grense for filtrering på ferdigstilt tidspunkt */
    val ferdigstiltTom: String? = null,
    /** Orgnr til organisasjonen oppgavene er opprettet for */
    val orgnr: List<String>? = null,
    /** Begrensning i antall returnerte oppgaver */
    val limit: Long? = 10,
    /** Offset for paginering i søk */
    val offset: Long? = null
)