# Copilot Instructions – historisk-superhelt

Saksbehandlingssystem for engangsutbetalinger på enkle helseytelser (ortoser, parykk, fottøy, proteser, reiseutgifter). Erstatter InfoTrygd-rutiner HT-MV, SB-SA og GE-PP.

## Arkitektur

```
root (Maven multi-module)
├── backend/          Spring Boot 4 + Kotlin – REST API + Kafka + JPA
├── frontend/         React 19 + TanStack Router/Query + Nav Aksel DS
├── libs/             Interne Kotlin-biblioteker (common-types, pdl, helved, tilgangsmaskin, dokarkiv, oppgave, pdfgen)
├── mocks/            Mock-server (kotlin server) + mock-oidc for lokal utvikling
├── pdfgen/           Brev-generator (Docker) 
└── e2e/              Playwright-tester (kjøres mot kjørende app)
```

**Flyten for en sak:**
1. Saksbehandler oppretter `Sak` → fyller inn opplysninger → sender til attestering
2. Attestant attesterer → sak ferdigstilles → `Vedtak` lagres
3. Ved innvilgelse sendes `Utbetaling` til Helved via Kafka (`historisk.utbetaling.v1`) og vedtaket journalføres i Dokarkiv og distribueres
4. Helved returnerer statuser tilbake på `helved.status.v1`

**Auth:** Azure AD via Nais Wonderwall (sidecar). Lokalt kjøres mock-oidc + Wonderwall i Docker. Texas brukes for token exchange til andre tjenester.

## Kommandoer

### Backend
```bash
mvn test                          # alle tester (fra rot eller backend/)
mvn test -Dtest=SakValidatorTest  # én testklasse
mvn compile                       # kompiler (kjøres fra rot for å bygge libs først)
```
Backend startes lokalt via `DevApplication.kt` i `backend/src/test/kotlin/`.
Krever `docker compose up` for Postgres, Kafka, mock-oidc og Wonderwall.

### Frontend
```bash
# i /frontend
npm start           # dev-server på :3000 (hot reload)
npm run test        # Vitest
npm run biome       # lint + format (Biome)
npm run biome:write # lint + format, skriv endringer
npm run openapi-ts  # generer typer fra backend-API (backend må kjøre)
```

### E2E
```bash
# i /e2e
npx playwright test       # headless
npx playwright test --ui  # med UI
npx playwright test -u    # oppdater snapshots
```

## Backend-konvensjoner

### Domeneoppsett – lag i backend
- **`*.kt`** – domenemodell (data class)
- **`db/`** – JPA-entity + JPA-repository
- **`rest/`** – REST-controller + DTO-typer
- **`*Repository.kt`** – app-repository (tilgangskontroll, mapping til domene)
- **`*Service.kt`** – forretningslogikk

### Tilgangskontroll
- `@PreAuthorize("hasAuthority('READ')")` / `'WRITE'` på repository-metoder
- `@tilgangsmaskin.harTilgang(#fnr)` for personbasert tilgang
- Aldri bypass auth-sjekker

### JPA / database
- Alltid Flyway-migrasjon for skjemaendringer – filnavnkonvensjon: `V{n}__{beskrivelse}.sql`
- **Aldri endre eksisterende migrasjonsfiler**
- Repository-klasser returnerer alltid domenemodell (ikke JPA-entity) utad
- `@Transactional` settes på service-metoder, ikke repository

### Kafka
- Producer: `UtbetalingKafkaProducer` sender `UtbetalingMelding` til Helved
- Consumer: `UtbetalingStatusConsumer` mottar statuser og oppdaterer `UtbetalingStatus`
- Status-maskin: `UTKAST → KLAR_TIL_UTBETALING → SENDT → MOTTATT → BEHANDLET → UTBETALT | FEILET`

### Tester
- Unit-tester bruker Assertj (`assertThat`) med JUnit 5
- Integrasjonstester bruker Testcontainers (Postgres) – krever Docker
- Testdata-fabrikker i `*TestData.kt`-objekter (eks. `SakTestData`, `UtbetalingTestData`)
- Mockito-kotlin for mocking av eksterne avhengigheter

## Frontend-konvensjoner

### Typing
- Typer genereres automatisk fra backend OpenAPI-spec med `npm run openapi-ts` → `generated/`
- Importer genererte typer via `@generated`-alias (eks. `import type { Sak } from "@generated"`)
- Lokale utility-typer utledes fra genererte typer: `type SakStatusType = Sak['status']`

### Routing og datahenting
- TanStack Router med filbasert routing i `src/routes/`
- TanStack Query for all datahenting (generert av openapi-ts)
- Shared komponenter i `src/common/`

### Styling og komponenter
- **Alltid** Nav Aksel Design System (`@navikt/ds-react`) – aldri custom CSS eller Tailwind padding/margin
- Spacing via `Box paddingBlock/paddingInline` med `space-*`-tokens, aldri Tailwind `p-*`/`m-*`
- Responsivt design med `{ xs: ..., md: ..., lg: ... }`-props

## Lokal utvikling – oppstartsrekkefølge

```bash
docker compose up          # Postgres, Kafka, Wonderwall, texas, mock-oidc
# Start DevApplication.kt i IDE (spring profile: dev)
cd frontend && npm start   # :3000 med hot reload
# Appen tilgjengelig på :4000 (gjennom Wonderwall med mock-auth)
```

Testpersoner fra mock-server (se `mocks/mock-server/README.md`):
- `70000000001` – person med dødsdato
- `60000000001` – strengt fortrolig adresse
- `40300000001` – avvises av tilgangsmaskin

## Libs-moduler

Interne Kotlin-libs (versjonert sammen med appen):
| Modul | Innhold |
|---|---|
| `common-types` | Verdi-typer: `Saksnummer`, `FolkeregisterIdent`, `Belop`, `NavIdent`, m.fl. |
| `pdl` | PDL-klient (personopplysninger) |
| `helved` | Kafka-meldingstyper for utbetaling |
| `tilgangsmaskin` | Tilgangskontroll-klient |
| `dokarkiv` | Journalføring (Dokarkiv + Dokdist + SAF) |
| `oppgave` | Oppgave-klient (Gosys) |
| `pdfgen` | Pdfgen-klient for brevgenerering |
