# Superhelt

Saksbehandling og engangsutbetaling på enkle helseytelser. Erstatter InfoTrygd-rutiner HT-MV, SB-SA og GE-PP.

> Alle trenger hjelp av en superhelt en gang i blant

## For fagpersoner

Superhelt er saksbehandlingssystemet for saksbehandlere og attestanter som behandler søknader om engangsutbetalinger på enkle helseytelser.

### Stønadstyper som støttes
- Ortoser
- Parykk
- Fottøy
- Proteser
- Reiseutgifter

### Roller
- **Saksbehandler** — oppretter og behandler saker
- **Attestant** — kvalitetssikrer og attesterer vedtak før utbetaling

### Klage og anke

Dersom bruker klager på et vedtak, kan saksbehandler sende klagen videre til klagebehandling i **Kabal** (Klage og Anke Behandling Av Alle saker) direkte fra Superhelt.

### Miljøer

| Miljø | URL |
|-------|-----|
| Dev (test) | https://superhelt.intern.dev.nav.no |
| Prod | https://superhelt.intern.nav.no |

---

## Arkitektur

```
root (Maven multi-module)
├── backend/    Spring Boot 4 + Kotlin – REST API, Kafka, JPA
├── frontend/   React 19 + TanStack Router/Query + Nav Aksel DS
├── libs/       Interne Kotlin-biblioteker (se tabell under)
├── mocks/      Mock-server + mock-oidc for lokal utvikling
├── pdfgen/     Brevgenerator (Docker)
└── e2e/        Playwright-tester (kjøres mot kjørende app)
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

### Saksflyten

1. Saksbehandler oppretter **Sak** → fyller inn opplysninger → sender til attestering
2. Attestant attesterer → sak ferdigstilles → **Vedtak** lagres
3. Ved innvilgelse sendes **Utbetaling** til Helved via Kafka (`historisk.utbetaling.v1`) og vedtaket journalføres i Dokarkiv og distribueres
4. Helved returnerer utbetalingsstatuser tilbake på `helved.status.v1`

**Auth:** Azure AD via Nais Wonderwall (sidecar). Token exchange til andre tjenester håndteres via Texas. Lokalt kjøres mock-oidc + Wonderwall i Docker.

![nettverksopplegg](./docs/super-docker-compose.png)

---

## Krav

| Verktøy | Versjon |
|---------|---------|
| Java | 25 |
| Node.js | ≥ 24 |
| pnpm | ≥ 10 |
| Docker | nyeste stabile |

> **Java 25** er påkrevd. Sett `JAVA_HOME` via sdkman (`sdk use java 25-...`) eller tilsvarende.

---

## Lokal utvikling

### Oppstartsrekkefølge

**1. Start avhengigheter (Postgres, Kafka, Wonderwall, Texas, mock-oidc):**
```shell
docker compose up
# eller:
make up
```

**2. Start backend** ved å kjøre [DevApplication.kt](./backend/src/test/kotlin/no/nav/historisk/superhelt/DevApplication.kt) i IDE.

Appen er tilgjengelig på http://localhost:4000 (gjennom Wonderwall med mock-auth).

**3. Start frontend:**
```shell
# i /frontend
pnpm start
```

Frontend er tilgjengelig på http://localhost:3000 med hot reload.  
Innlogging skjer via http://localhost:4000 (Wonderwall håndterer auth – `:3000` alene vil ikke fungere).

### Make-kommandoer

```shell
make up      # start alle avhengigheter
make down    # stopp alle avhengigheter
make test    # kjør alle tester
make build   # bygg applikasjon og Docker-images
make all     # bygg, test og bygg Docker-images
```

### Tester

**Backend** – krever Docker kjørende (TestContainers):
```shell
mvn test
# eller:
make test
```
For Colima på Mac, se https://golang.testcontainers.org/system_requirements/using_colima/

**Frontend:**
```shell
# i /frontend
pnpm run test
```

**E2E** – krever at hele appen kjører:
```shell
# i /e2e
pnpm playwright:install     # første gang – installer nettlesere
pnpm playwright:test        # headless
pnpm playwright:test:ui     # med UI
```
Se [e2e/README.md](./e2e/README.md) for mer om Playwright-oppsett.

### Testpersoner

Mock-serveren har forhåndsdefinerte testpersoner. Se [mocks/mock-server/README.md](./mocks/mock-server/README.md) for fullstendig oversikt.

### Formatering og linting (frontend)

```shell
# i /frontend
pnpm run biome        # sjekk
pnpm run biome:write  # sjekk og skriv endringer
```
