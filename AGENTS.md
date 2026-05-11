# AGENTS.md — historisk-superhelt

Saksbehandlingssystem for engangsutbetalinger på enkle helseytelser (ortoser, parykk, fottøy, proteser, reiseutgifter). Erstatter InfoTrygd-rutiner HT-MV, SB-SA og GE-PP. To roller: **saksbehandler** (oppretter/behandler) og **attestant** (kvalitetssikrer).

## Build & Test Commands

### Backend (Maven multi-module, kjør fra rot eller `backend/`)
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
pnpm start             # dev-server på :3000 (hot reload)
pnpm run test          # Vitest
pnpm run biome         # lint + format
pnpm run biome:write   # lint + format, skriv endringer
pnpm run openapi-ts    # generer typer fra backend-API (backend må kjøre)
```
> Lint kun endrede filer i den aktive sesjonen: `npx biome check <fil>`

### E2E (kjør fra `e2e/` — krever at hele appen kjører)
```bash
pnpm playwright:install         # første gang – installer nettlesere
pnpm playwright:test            # headless
pnpm playwright:test:ui         # med UI
pnpm playwright:snapshot-update # oppdater snapshots
pnpm playwright:codegen http://localhost:4000  # opptak av ny test
```

## Requirements

| Verktøy | Versjon |
|---------|---------|
| Java    | 25      |
| Node.js | ≥ 24    |
| pnpm    | ≥ 10    |
| Docker  | nyeste stabile |

## Project Structure

```text
backend/          Spring Boot 4 + Kotlin – REST API + Kafka + JPA
  src/main/.../superhelt/
    sak/          Sak-domene (Sak, SakService, SakRepository, SakValidator)
    vedtak/       Vedtak-domene
    utbetaling/   Utbetaling-domene + Kafka-producer/consumer
    brev/         Fritekst- og malbrev (pdfgen-integrasjon)
    klage/        Kabal-integrasjon for klagebehandling
    statistikk/   Kafka-producer for statistikk
    infotrygd/    InfoTrygd-integrasjon
    ansatt/       NavAnsatt + Enheter
    infrastruktur/ Auth, MDC, permission, exception-handling
    StonadsType.kt  Enum over alle stønadstypene
docs/
e2e/              Playwright-tester
frontend/         React 19 + TanStack Router/Query + Nav Aksel DS
  src/routes/
    sak/$saksnummer/  Saksskjerm (opplysninger, oppsummering, vedtaksbrev, klage)
    oppgave/          Oppgavevisning
    person/           Personsøk
libs/             Interne Kotlin-biblioteker
mocks/            Mock-server + mock-oidc for lokal utvikling
pdfgen/           Brevgenerator (Docker)
```

### Interne libs

| Modul | Innhold |
|---|---|
| `common-types` | Verdi-typer: `Saksnummer`, `FolkeregisterIdent`, `Belop`, `NavIdent` m.fl. |
| `pdl` | PDL-klient (personopplysninger) |
| `helved` | Kafka-meldingstyper for utbetaling |
| `tilgangsmaskin` | Tilgangskontroll-klient |
| `dokarkiv` | Journalføring (Dokarkiv + Dokdist + SAF) |
| `oppgave` | Oppgave-klient (Gosys) |
| `pdfgen` | Pdfgen-klient for brevgenerering |
| `kabal` | Klient for oversending av klagesaker til Kabal |
| `infotrygd` | Klient mot InfoTrygd |
| `ereg` | Klient mot Enhetsregisteret |
| `entra-proxy` | Entra ID-proxy for tjenestekommunikasjon |
| `statistikk` | Kafka-meldingstyper for statistikk |

## Architecture

### Sagsflyt
1. Saksbehandler oppretter `Sak` → fyller inn opplysninger → sender til attestering
2. Attestant attesterer → sak ferdigstilles → `Vedtak` lagres
3. Ved innvilgelse sendes `Utbetaling` til Helved via Kafka (`historisk.utbetaling.v1`) og vedtaket journalføres og distribueres via Dokarkiv
4. Helved returnerer statuser på `helved.status.v1`
5. Statistikk publiseres på en egen Kafka-topic via `SakStatistikkKafkaProducer`
6. Klager sendes til Kabal og statuser mottas via `KabalBehandlingEventConsumer`

### Status-maskiner
- **SakStatus**: `UNDER_BEHANDLING → TIL_ATTESTERING → FERDIG_ATTESTERT → FERDIG | FEILREGISTRERT`
- **UtbetalingStatus**: `UTKAST → KLAR_TIL_UTBETALING → SENDT_TIL_UTBETALING → MOTTATT_AV_UTBETALING → BEHANDLET_AV_UTBETALING → UTBETALT | FEILET`
- **BrevStatus**: `NY → UNDER_ARBEID → KLAR_TIL_SENDING → SENDT`

### Auth
Azure AD via Nais Wonderwall (sidecar). Texas brukes for token exchange til andre tjenester. Lokalt kjøres mock-oidc + Wonderwall i Docker.

## Local Development

Oppstartsrekkefølge:
```bash
docker compose up                 # Postgres, Kafka, Wonderwall, Texas, mock-oidc
# Start DevApplication.kt i IDE (backend/src/test/kotlin/.../DevApplication.kt)
cd frontend && pnpm start         # :3000 med hot reload
# Appen tilgjengelig på :4000 (gjennom Wonderwall med mock-auth)
```

### Testpersoner (mock-server)

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

## Code Style

### Minimal Editing

When fixing a bug or implementing a feature, change only what is necessary.
Do not rename variables, restructure working code, or refactor beyond the task at hand.
Keep diffs small and focused so they are easy to review.

### Backend layer conventions

Each domain package follows this layout:
- `*.kt` — domenemodell (data class)
- `db/` — JPA-entity + JPA-repository
- `rest/` — REST-controller + DTO-typer
- `*Repository.kt` — app-repository (tilgangskontroll, mapping til domene)
- `*Service.kt` — forretningslogikk

Rules:
- `@Transactional` on service methods, not repository
- `@PreAuthorize("hasAuthority('READ')")` / `'WRITE'` on repository methods
- JPA-entity never exposed outside the repository layer
- Flyway migrations: `V1.{nnn}__{beskrivelse}.sql` — **never modify existing files**
- Testdata-fabrikker in `*TestData.kt` objects (e.g. `SakTestData`, `UtbetalingTestData`, `VedtakTestData`)

### Frontend conventions

- All types generated from backend OpenAPI-spec: `pnpm run openapi-ts` → `generated/`
- Import generated types via `@generated` alias: `import type { Sak } from "@generated"`
- Use Nav Aksel DS (`@navikt/ds-react`) — never custom CSS, Tailwind padding/margin
- Spacing via `Box paddingBlock/paddingInline` with `space-*` tokens (e.g. `"space-16"`)
- `gap` on `VStack`/`HStack`/`HGrid` also requires `space-*` prefix (e.g. `gap="space-4"`)
- Desktop-first layout — `lg`/`md` as primary breakpoints, not `xs`

## Git Workflow

<!-- TODO: Document your branching and merge strategy -->

## Boundaries

### ✅ Always

- Run tests after changes
- Follow existing code patterns in the project
- Preserve existing code structure — do not reorganize or refactor beyond the task
- Validate all external input

### ⚠️ Ask First

- Changing authentication mechanisms
- Adding new dependencies
- Modifying database schema

### 🚫 Never

- Commit secrets or credentials
- Skip input validation on external boundaries
