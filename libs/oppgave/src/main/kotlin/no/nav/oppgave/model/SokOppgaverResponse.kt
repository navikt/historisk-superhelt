package no.nav.oppgave.model

data class SokOppgaverResponse(
    val antallTreffTotalt: Long? = null,
    val oppgaver: List<Oppgave>? = null
)