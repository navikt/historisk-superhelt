package no.nav.kabal.model

/**
 * Event-typer fra Kabal på topic behandling-events.v1.
 *
 * [skalLageOppgaveVedUtfall] angir om event-typen skal trigge opprettelse av en oppgave
 * basert på utfallet. Kun AVSLUTTET-event-typer skal sjekke utfall – OPPRETTET-events
 * har ikke et endelig utfall å handle på.
 *
 * Se også [KabalUtfall.lagOppgave] for utfall-spesifikk filtrering.
 */
enum class KabalBehandlingEventType(val skalLageOppgaveVedUtfall: Boolean = false) {
    KLAGEBEHANDLING_AVSLUTTET(skalLageOppgaveVedUtfall = true),
    ANKEBEHANDLING_OPPRETTET,
    ANKEBEHANDLING_AVSLUTTET(skalLageOppgaveVedUtfall = true),
    /** Innstilling sendt til Trygderetten – har et valgfritt utfall, men er ikke en endelig avgjørelse.
     *  Sjekk av lagOppgave på utfall er ikke relevant her. */
    ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
    /** Feilregistrert behandles separat – oppretter alltid oppgave med egen tekst uavhengig av utfall. */
    BEHANDLING_FEILREGISTRERT,
    BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET(skalLageOppgaveVedUtfall = true),
    OMGJOERINGSKRAVBEHANDLING_AVSLUTTET(skalLageOppgaveVedUtfall = true),
    GJENOPPTAKSBEHANDLING_AVSLUTTET(skalLageOppgaveVedUtfall = true),
}

