package no.nav.oppgave.model

data class SokOppgaverResponse(
    /** Totalt antall oppgaver funnet med dette søket */
    val antallTreffTotalt: Long? = null,
    /** Liste over oppgaver */
    val oppgaver: List<Oppgave>? = null
)