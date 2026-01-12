package no.nav.oppgave.model

/** Hvilken status oppgaven har. Konsumenter bør kun forholde seg til dette ved behov for å skille mellom ferdigstilt og feilregistrert */
enum class Status {
    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT
}