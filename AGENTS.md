# AGENTS.md

## Prosjekt

Historisk Superhelt behandler engangsutbetalinger for enkle helseytelser og erstatter InfoTrygd-rutinene HT-MV, SB-SA og GE-PP.

- `backend/`: Kotlin og Spring Boot, REST API, Kafka og JPA
- `frontend/`: React, TanStack Router/Query og Nav Aksel
- `libs/`: interne Kotlin-biblioteker for PDL, Helved, tilgangsmaskin, Dokarkiv, Gosys, Pdfgen, Kabal, InfoTrygd, Ereg, Entra-proxy, statistikk og felles typer
- `mocks/`: mock-server og mock-OIDC
- `pdfgen/`: brev-generator
- `e2e/`: Playwright-tester

## Saksflyt og integrasjoner

Saksbehandler oppretter og fyller ut en sak, sender den til attestering, og vedtaket lagres etter attestering. Innvilgede vedtak journalføres i Dokarkiv og utbetalinger sendes til Helved. Klager sendes til Kabal.

| Kafka-topic | Bruk |
|---|---|
| `historisk.utbetaling.v1` | Utbetalinger til Helved |
| `helved.status.v1` | Status fra Helved |
| `historisk.superhelt.statistikk.saksbehandling.v1` | Saksbehandlingsstatistikk |
| `klage.behandling-events.v1` | Behandlingshendelser fra Kabal |

Utbetalingsstatus: `UTKAST` → `KLAR_TIL_UTBETALING` → `SENDT` → `MOTTATT` → `BEHANDLET` → `UTBETALT` eller `FEILET`.

## Arkitektur og kodestandard

Før du endrer backend-kode eller tester, les `docs/arkitektur-og-kodestandard.md`. Der står reglene for lagdeling, validering, transaksjonsgrenser og testdekning, adskilt fra dagens praksis.

## Backend

- Autentisering skjer via Azure AD og Nais Wonderwall. Texas brukes til token exchange.
- Behold tilgangssjekker: bruk `@PreAuthorize` og personbasert tilgangskontroll der det kreves. Ikke logg fødselsnummer eller andre personopplysninger.
- Skjemaendringer krever ny Flyway-migrasjon (`V{n}__beskrivelse.sql`). Ikke endre eksisterende migrasjoner.

## Frontend

- Bruk genererte typer og klienter fra `@superhelt/api`, og React Query-funksjoner fra `@superhelt/api/react-query`.
- Bruk TanStack Router for filbasert routing og TanStack Query for datahenting.
- Bruk Nav Aksel (`@navikt/ds-react`). Bruk `Box` og `space-*`-tokens for spacing, ikke Tailwind margin- eller padding-klasser.
- Kjør Biome bare på filer som er endret i oppgaven.

## Lokal utvikling og kommandoer

Start avhengigheter med `docker compose up`, start `DevApplication.kt` med `dev`-profilen, 
og kjør `pnpm frontend:start`. Pålogging må skje på http://localhost:4000
Appen nås på `http://localhost:4000` via Wonderwall.
dev server nås på `http://localhost:3000` etter at browser er autentisert.

```bash
mvn test
mvn test -pl backend -am -Dtest=SakValidatorTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm test
pnpm typecheck
pnpm frontend:build
pnpm e2e:test
```

Backend-tester krever Docker. API-typer genereres med `pnpm api:generate` mens backend kjører. E2E-tester krever at appen kjører.

## Agent skills

### Issue tracker

Issues spores i GitHub Issues. Les `docs/agents/issue-tracker.md` før du leser, oppretter eller endrer issues.

### Triage labels

Vi bruker standardetikettene. Les `docs/agents/triage-labels.md` før triage eller endring av triage-etiketter.

### Domain docs

Vi bruker single-context med felles ordliste og ADR-er. Les `docs/agents/domain.md` før du utforsker kodebasen.
