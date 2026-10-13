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

## Backend

- Domenepakker ligger under `backend/src/main/kotlin/no/nav/historisk/superhelt/`.
- `db/` inneholder JPA-entities og JPA-repositories, `rest/` inneholder controllere og DTO-er. App-repositories mapper til domenemodeller; services inneholder forretningslogikk.
- Autentisering skjer via Azure AD og Nais Wonderwall. Texas brukes til token exchange.
- Behold tilgangssjekker: bruk `@PreAuthorize` og personbasert tilgangskontroll der det kreves. Ikke logg fødselsnummer eller andre personopplysninger.
- Skjemaendringer krever ny Flyway-migrasjon (`V{n}__beskrivelse.sql`). Ikke endre eksisterende migrasjoner.
- Ikke eksponer JPA-entities utenfor repository-laget. Sett `@Transactional` på service-metoder.

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

## Tester

- Skriv tester for ny funksjonalitet, inkludert suksess- og feiltilfeller.
- Bruk beskrivende testnavn som uttrykker forventet oppførsel.
- Bruk AssertJ og JUnit 5 for Kotlin-tester, Testcontainers for integrasjonstester og Playwright for brukerflyter.
- Ikke hopp over tester eller endre teststruktur uten avklaring.
