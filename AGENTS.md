# AGENTS.md — historisk-superhelt

> Fullstendig prosjektbeskrivelse, konvensjoner og arkitektur ligger i `.github/copilot-instructions.md`.
> Denne filen inneholder kun tilleggsinformasjon som er spesifikk for autonome agenter.

## Architecture Supplements

### Libs-moduler (komplett liste — erstatter tabellen i copilot-instructions.md)

| Modul | Innhold |
|---|---|
| `common-types` | Verdi-typer: `Saksnummer`, `FolkeregisterIdent`, `Belop`, `NavIdent` m.fl. |
| `pdl` | PDL-klient (personopplysninger) |
| `helved` | Kafka-meldingstyper for utbetaling |
| `tilgangsmaskin` | Tilgangskontroll-klient |
| `dokarkiv` | Journalføring (Dokarkiv + Dokdist + SAF) |
| `oppgave` | Oppgave-klient (Gosys) |
| `pdfgen` | Pdfgen-klient for brevgenerering |
| `kabal` | Klient for oversending av klagesaker til Kabal (klage/anke) |
| `infotrygd` | Klient mot InfoTrygd |
| `ereg` | Klient mot Enhetsregisteret (samhandler-oppslag) |
| `entra-proxy` | Entra ID-proxy for tjenestekommunikasjon |
| `statistikk` | Kafka-meldingstyper for saksbehandlingsstatistikk |

### Backend-domener (under `backend/src/main/kotlin/no/nav/historisk/superhelt/`)

| Pakke | Ansvar |
|---|---|
| `sak/` | Saksdomenets kjerne |
| `vedtak/` | Vedtak og attestering |
| `utbetaling/` | Utbetalingshåndtering (Helved-integrasjon) |
| `klage/` | Klage/anke — oversendelse til Kabal, mottar events tilbake |
| `brev/` | Brevgenerering via Pdfgen |
| `person/` | Personopplysninger via PDL |
| `ansatt/` | NAV-ansatt-informasjon |
| `oppgave/` | Gosys-oppgaver |
| `samhandler/` | Samhandler-oppslag via Ereg |
| `statistikk/` | Sender saksbehandlingsstatistikk til Kafka |
| `infotrygd/` | InfoTrygd-integrasjon |
| `dokarkiv/` | Journalføring |

### Kafka-topics (komplett)

| Topic | Retning | Beskrivelse |
|---|---|---|
| `historisk.utbetaling.v1` | Producer | Utbetalingsmeldinger til Helved |
| `helved.status.v1` | Consumer | Utbetalingsstatuser fra Helved |
| `historisk.superhelt.statistikk.saksbehandling.v1` | Producer | Saksbehandlingsstatistikk |
| `klage.behandling-events.v1` | Consumer | Behandlingsevents fra Kabal |

### Klage/anke-flyt

1. Saksbehandler sender klage fra Superhelt → `KlageService` → `KabalClient` → Kabal API
2. Kabal sender behandlingsstatus tilbake på `klage.behandling-events.v1`
3. `KabalBehandlingEventConsumer` prosesserer events og oppdaterer `KabalEventRepository`

## Build & Test Commands

### Backend
```bash
mvn test                          # alle tester
mvn test -Dtest=SakValidatorTest  # én testklasse
mvn clean package                 # bygg
make test                         # tester via Makefile
make build                        # bygg applikasjon og Docker-images
make all                          # bygg, test og Docker-images
make up                           # start docker-avhengigheter
make down                         # stopp docker-avhengigheter
```
> Backend-tester krever Docker kjørende (Testcontainers). For Colima på Mac: https://golang.testcontainers.org/system_requirements/using_colima/

### Frontend (kjør fra `frontend/`)
```bash
pnpm start             # dev-server på :3000
pnpm run test          # Vitest
pnpm run biome:write   # lint + format
pnpm run openapi-ts    # generer typer (backend må kjøre)
```

### E2E (kjør fra `e2e/` — krever at hele appen kjører)
```bash
pnpm playwright:install                            # første gang
pnpm playwright:test                               # headless
pnpm playwright:test:ui                            # med UI
pnpm playwright:snapshot-update                    # oppdater snapshots
pnpm playwright:codegen http://localhost:4000       # opptak av ny test
```

## Requirements

| Verktøy | Versjon |
|---------|---------|
| Java    | 25      |
| Node.js | ≥ 24    |
| pnpm    | ≥ 10    |
| Docker  | nyeste stabile |

## Testpersoner (mock-server)

| FNR | Beskrivelse |
|-----|-------------|
| `70000000001` | Person med dødsdato (2023-06-15) |
| `60000000001` | Strengt fortrolig adresse (godkjent av tilgangsmaskin) |
| `60000000002` | Fortrolig adresse |
| `60000000003` | Strengt fortrolig adresse utland |
| `40400000000` | Ukjent person (avvises av tilgangsmaskin) |
| `40300000001` | Avvises pga. habilitet |
| `40300000002` | Avvises pga. dødsdato |
| `40300000006` | Avvises pga. strengt fortrolig adresse |
| `40300000007` | Avvises pga. fortrolig adresse |
| `7000000002`  | Person med verge (NAV-tjenestevirksomhet, hjelpemidler) |
| Andre 11-siffer FNR | Genererer tilfeldig person |
