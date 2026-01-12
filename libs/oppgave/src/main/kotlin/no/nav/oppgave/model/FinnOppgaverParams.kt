package no.nav.oppgave.model

data class FinnOppgaverParams(
    val statuskategori: String? = null,
    val tema: List<String>? = null,
    val oppgavetype: List<String>? = null,
    val tildeltEnhetsnr: String? = null,
    val tilordnetRessurs: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val aktoerId: List<String>? = null,
    val journalpostId: List<String>? = null,
    val saksreferanse: List<String>? = null,
    val ferdigstiltFom: String? = null,
    val ferdigstiltTom: String? = null,
    val orgnr: List<String>? = null,
    val limit: Long? = 10,
    val offset: Long? = null
)