# Arkitektur og kodestandard

Denne guiden samler systemoversikten og den avklarte kodestandarden i Historisk Superhelt. Detaljer om dagens kode og kjente avvik står i egne beskrivelser av [frontend](frontend-arkitektur.md) og [backend](backend-arkitektur.md). Dokumentene forklarer ikke hvorfor tidligere arkitekturvalg ble gjort.

## Hvordan lese guiden

- **Regel** betyr et krav i denne guiden, prosjektets øvrige instrukser eller verktøykonfigurasjon.
- **Observert praksis** betyr et mønster i de undersøkte kodeeksemplene, ikke et krav om at all kode skal følge det.
- **Avvik** betyr en konkret forskjell mellom en regel eller domenedefinisjon og implementasjonen.

[AGENTS.md](../AGENTS.md) peker til denne guiden og inneholder øvrige prosjektinstrukser. Reglene for lagdeling, validering, transaksjonsgrenser og testdekning står her. [GLOSSARY.md](../GLOSSARY.md) definerer domenebegrepene.

| Når du arbeider med | Les videre |
|--------------------|------------|
| Ruter, Query-cache, API-klient, redigering og breveditor | [Frontend-arkitektur](frontend-arkitektur.md) |
| Domenemodell, persistens, transaksjoner, tilgang og integrasjoner | [Backend-arkitektur](backend-arkitektur.md) |

## Arkitekturoversikt

Systemet er et Maven- og pnpm-basert repo med en Spring Boot-applikasjon, React-frontend, interne integrasjonsbiblioteker og en separat brevtjeneste.

| Område | Ansvar og kodeeksempel |
|--------|-----------------------|
| `backend/` | Kotlin/Spring Boot, REST, forretningsflyt, JPA og Kafka. Eksempel: [sakspakken](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/) |
| `frontend/` | React-grensesnitt med TanStack Router/Query og Nav Aksel. Eksempel: [saksruten](../frontend/src/routes/sak/$saksnummer/route.tsx) |
| `libs/` | Interne Kotlin-moduler for integrasjoner og felles typer. Modulene listes i [libs/pom.xml](../libs/pom.xml) |
| `packages/` | Delte frontend-pakker, blant annet generert API-klient og breveditor. Se [pakkeoversikten](../packages/README.md) |
| `pdfgen/` | Separat Typst-basert brevtjeneste kjørt med `pdfgenrs`. Se [pdfgen/README.md](../pdfgen/README.md) |
| `mocks/` | Mock-server og mock-OIDC for lokal utvikling og tester. Se [mock-serverens README](../mocks/mock-server/README.md) |
| `e2e/` | Playwright-tester av brukerflyter. Eksempel: [saksbehandling og attestering](../e2e/end-to-end/saksbehandling-attestert.spec.ts) |
| `.nais/` | Deployment, database, Kafka og tilgang til integrasjoner. Se [backend-manifestet](../.nais/app/backend.yaml) |

### Bygg- og driftsgrenser

Frontenden er en egen byggmodul, men deployes sammen med backend. [Frontendens Maven-oppsett](../frontend/pom.xml) bygger React-appen og pakker `dist` som statiske ressurser. [Backendens Maven-oppsett](../backend/pom.xml) avhenger av frontend-modulen, og Spring Boot serverer ressursene fra `classpath:/static`.

[Backendens Dockerfile](../backend/Dockerfile) pakker JAR-en i ett image. Frontend og backend leveres derfor i samme image. Pdfgen har [eget Nais-manifest](../.nais/app/pdfgen.yaml) og [egen workflow](../.github/workflows/pdfgen.yml).

Maven-modulene defineres i [pom.xml](../pom.xml). Node-workspacet defineres i [pnpm-workspace.yaml](../pnpm-workspace.yaml). Disse er ulike bygginndelinger i samme repo.

### Domenet og dagens forenkling

Ordlisten skiller mellom søknad, sak, krav, behandling, vedtak og utbetaling. En søknad kan inneholde flere krav, og hvert vedtak gjelder ett krav.

Kravets utfall er trukket, vedtatt eller omgjort. Et krav under arbeid har ennå ikke noe utfall. Vedtatt og omgjort fører til vedtak med vedtaksresultatet innvilget, delvis innvilget eller avslått, mens trukket ikke fører til vedtak.

Flere vedtak kan underrettes i samme vedtaksbrev etter at behandlingen er avsluttet. Ved omgjøring erstatter det nye vedtaket det gamle for samme krav, men begge beholdes. Feilregistrering gjelder saken, ikke kravet, og fører ikke til vedtak.

Dagens modell forenkler dette til ett resultat og ett beløp per behandling. Se [backendens domenemodell](backend-arkitektur.md#ansvarsgrenser-og-domenemodell) for behandlingsteller, vedtakskopier og databasebegrensninger. Disse forenklingene er ikke domeneregler.

## En ende-til-ende-flyt

Godkjent attestering viser hvordan UI, REST, tjenester og Kafka henger sammen:

1. [AttesterSakAction](../frontend/src/routes/sak/$saksnummer/-components/AttesterSakAction.tsx) bruker React Query-mutasjonen `attersterSakMutation` fra den genererte API-pakken. Den sender godkjenning og eventuell kommentar.
2. Den [genererte SDK-en](../packages/superhelt-api/src/sdk.gen.ts) sender `PUT /api/sak/{saksnummer}/status/attester`.
3. [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) validerer handlingen og setter ved godkjenning saken til `FERDIG_ATTESTERT`.
4. Ferdigstillingen sender eventuelt brev gjennom backendens brev- og pdfgen-integrasjon til journalføring og distribusjon. Deretter sendes eventuell utbetaling til Helved via Kafka.
5. Backend setter saken til `FERDIG`, lagrer vedtaket og ferdigstiller attestantoppgaven.
6. Frontenden starter invalidasjon av saksdata etter godkjent serverkall. Helved-status kommer senere inn via Kafka og oppdaterer utbetalingen separat.

Flyten er ikke én felles transaksjon. Se [frontendens attestering](frontend-arkitektur.md#attestering-fra-brukergrensesnittet) for pending-, feil- og cachehåndtering og [backendens ferdigstilling](backend-arkitektur.md#attestering-og-ferdigstilling) for rekkefølge og sideeffekter.

### Kommunikasjonsformer

Frontend bruker REST via den genererte klienten. Backend bruker både synkrone integrasjonsklienter og Kafka. Integrasjonsbibliotekene ligger i `libs/`, mens applikasjonens integrasjonsoppsett og flyt ligger i backend.

| Kafka-topic | Bruk |
|-------------|------|
| `historisk.utbetaling.v1` | Utbetalingsmeldinger til Helved |
| `helved.status.v1` | Utbetalingsstatus fra Helved |
| `historisk.superhelt.statistikk.saksbehandling.v1` | Saksbehandlingsstatistikk |
| `klage.behandling-events.v1` | Behandlingshendelser fra Kabal |

Se [topic-manifestet](../.nais/app/topics.yaml), [backend-manifestet](../.nais/app/backend.yaml) og [prosjektoversikten](../AGENTS.md). Kabal-behandlinger er eksterne behandlingsforløp, ikke automatisk nye behandlinger i Superhelt.

## Kodestandard

### Løkarkitektur i backend

Backend følger løkarkitektur med DDD som utgangspunkt. Domenet er kjernen og uttrykker begreper og forretningsregler. Applikasjonslaget orkestrerer brukstilfeller rundt domenet. Ytre adaptere håndterer HTTP, persistens og eksterne tjenester.

Kodeavhengighetene skal som hovedregel peke innover:

```text
Ytre adaptere → Applikasjonslag → Domene
```

Domeneobjekter skal ikke kjenne database, JPA, controllere eller HTTP-responser. Mapping til persistens- og transportmodeller hører hjemme utenfor domenet. Dette er en regel om avhengigheter, ikke et krav om bestemte mapper eller et eget interface og en adapterklasse for hver operasjon.

Enkle serialiseringsannotasjoner, som `@JsonProperty`, er et akseptert unntak så lenge de ikke påvirker forretningsreglene eller former domenemodellen etter et eksternt API. Egne DTO-er, interfaces og ekstra lag innføres ved konkret behov for å beskytte domenets uavhengighet, ikke som et absolutt krav. Dette avveier domenets uavhengighet mot unødvendig mapping og boilerplate, i tråd med KISS og YAGNI.

### Ansvarsdeling i backend

**Regler:** Domenepakker ligger under `backend/src/main/kotlin/no/nav/historisk/superhelt/`. `db/` inneholder JPA-entities og JPA-repositories. `rest/` inneholder controllere og REST-DTO-er. JPA-entities skal ikke eksponeres utenfor repository-laget.

Dette er avklart standard for backend-kode, ikke en påstand om at alle eksisterende flyter følger den.

| Lag | Ansvar |
|-----|--------|
| Controller | Håndterer HTTP, request-validering og respons. Delegerer brukstilfellet til en service |
| Service | Orkestrerer brukstilfellet i applikasjonslaget og håndhever forretningsregler gjennom domenetyper eller validatorer |
| Repository | Oppslag, lagring og mapping mellom persistens- og domenetyper |

Avgjørelser om tillatte handlinger og statusoverganger hører til service-/domenelaget, ikke repositoryets hovedansvar. Databasen håndhever fortsatt sine restriksjoner.

Standarden innebærer ikke at eksisterende tilgangskontroll skal fjernes eller flyttes automatisk. Eventuell flytting av slike sjekker må vurderes separat.

### DTO-er, validering og feil

**Regler:** Controlleren validerer request-format og påkrevde felt. Service-/domenelaget validerer om handlingen er tillatt ut fra sakens tilstand, også når inngangen ikke er HTTP. Domeneregler kan samles i egne validatorer som servicen bruker.

Se [backendens validering og REST-feil](backend-arkitektur.md#lagdeling-validering-og-rest-feil) for dagens DTO-er, validator og sentrale feilhåndtering.

### Database og transaksjoner

**Regler:** Skjemaendringer krever en ny Flyway-migrasjon. Eksisterende migrasjoner skal ikke endres.

Vurder transaksjonsgrensen for hver operasjon. Skill lokale databaseendringer fra eksterne sideeffekter. Ikke legg som standard én `@Transactional` rundt hele serviceflyten. For operasjoner som sender brev eller betaling, må teamet ha avklart hva som skjer ved delvis feil og nye forsøk.

Se backendens beskrivelser av [ferdigstilling](backend-arkitektur.md#attestering-og-ferdigstilling), [brevsending](backend-arkitektur.md#brev-journalføring-og-distribusjon) og [utbetaling](backend-arkitektur.md#utbetaling-og-status-fra-helved) før endringer i disse grensene.

### Autentisering og tilgangskontroll

**Regler:** Behold nødvendige tilgangssjekker, og ikke logg fødselsnummer eller andre personopplysninger. En generell autentiseringsregel erstatter ikke personbasert tilgangskontroll.

Se [backendens tilgangsgrenser](backend-arkitektur.md#autentisering-og-tilgangsgrenser) for innlogging, person-/temakontroll og utgående tokenhåndtering.

### Frontend

**Regler:** Bruk TanStack Router for filbasert routing, TanStack Query for datahenting og genererte typer og klienter fra `@superhelt/api`. Oppdater genererte filer gjennom generatoren. Bruk Nav Aksel med `Box` og `space-*`-tokens for spacing, ikke Tailwind margin- og padding-klasser.

Se [frontend-arkitekturen](frontend-arkitektur.md) for routing, API-exports, Query-cache, feil og redigering.

## Tester og verktøy

### Ønsket testdekning

**Regler:** Test funksjonalitet, ikke implementasjonsdetaljer. Bruk beskrivende testnavn og dekk relevante suksess- og feiltilfeller ved ny eller endret funksjonalitet.

Samme atferd skal normalt dekkes på ett testnivå. Hvis en API-test allerede dekker atferden, skal den ikke testes på nytt i service- eller repository-laget. Ekstra tester skal dekke en egen risiko eller et tilfelle som ikke allerede er dekket. Trivielle gettere og settere trenger ikke tester.

Velg testnivå etter hva som skal verifiseres:

| Ansvar | Egnet testnivå når dekning mangler |
|--------|-----------------------------------|
| Forretningsregler og orkestrering | Service-/domenetester |
| Persistens, mapping og databaserestriksjoner | Repository-tester |
| Request-validering, HTTP-respons og tilgangskontroll | HTTP-tester |
| Sammenhengende brukerflyt | E2E-tester |

Tabellen er ikke et krav om å teste hver operasjon på alle nivåer. Test relevante delvise feil og nye forsøk for operasjoner med eksterne sideeffekter, uten å gjenta atferd som allerede er testet.

Bruk JUnit 5 og AssertJ for Kotlin-tester, Testcontainers for integrasjonstester og Playwright for brukerflyter. Ikke hopp over tester eller endre teststruktur uten avklaring.

Testoppsett, representative eksempler og begrensninger beskrives sammen med [frontend](frontend-arkitektur.md#tester-og-kjente-avvik) og [backend](backend-arkitektur.md#database-drift-og-tester).

### Hva verktøyene håndhever

[Biome-konfigurasjonen](../biome.json) angir blant annet fire mellomrom, linjebredde 120 og anbefalte lint-regler. [Frontendens Biome-konfigurasjon](../frontend/biome.json) arver root-oppsettet og ekskluderer genererte `.gen.ts`-filer.

**Regel:** Kjør Biome bare på filer som er endret i oppgaven. Root-scriptet `pnpm biome` dekker hele konfigurasjonens omfang; det er ikke en avgrenset sjekk av endrede filer.

I de undersøkte Maven- og CI-konfigurasjonene er det ikke funnet Kotlin-formattering med Spotless/ktlint, detekt eller arkitekturtester. Kotlin-navnemønstrene ovenfor skal derfor ikke leses som regler håndhevet av en slik sjekk.

### Vanlige kommandoer

Kjør kommandoene fra repo-roten. Verktøyversjoner styres av [pom.xml](../pom.xml) og [package.json](../package.json). Se [README](../README.md#krav) for verktøykrav og [prosjektinstruksene](../AGENTS.md#lokal-utvikling-og-kommandoer) for lokal oppstart.

| Oppgave | Kommando |
|---------|----------|
| Installer workspace-avhengigheter fra låsefilen | `pnpm install --frozen-lockfile` |
| Bygg og verifiser Maven-modulene, inkludert frontend | `mvn clean verify` |
| Bygg uten tester | `mvn package -DskipTests` |
| Kjør Maven-tester | `mvn test` |
| Én backend-testklasse med nødvendige moduler | `mvn test -pl backend -am -Dtest=SakValidatorTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Unit-tester i pnpm-workspaces | `pnpm test` |
| Én frontend-testfil | `pnpm --filter super-frontend test src/common/dato.utils.test.ts` |
| Typekontroll i workspaces | `pnpm typecheck` |
| Produksjonsbygg av frontend | `pnpm frontend:build` |
| Generer API-klienten | `pnpm api:generate` |
| Installer Chromium for browser-tester | `pnpm test:browser:install` |
| Browser-tester | `pnpm test:browser` |
| Én browser-testfil | `pnpm --filter @superhelt/breveditor test:browser src/HtmlPdfgenEditor.browser.test.tsx` |
| Alle E2E-tester | `pnpm e2e:test` |
| Én E2E-testfil | `pnpm e2e:test end-to-end/henlegg.spec.ts` |

Filstiene i tabellen er eksempler; erstatt dem med testfilene du skal kjøre. Testkommandoene er definert i workspace-pakkenes `package.json`.

`pnpm test` inkluderer ikke browser- eller integrasjonsprosjektene. Browser-tester krever Chromium. Backend-integrasjonstester bruker Testcontainers og krever Docker. For pdfgen-tester og deres forutsetninger, se [pdfgen/AGENTS.md](../pdfgen/AGENTS.md#tester).

Maven-modulen `frontend` kjører pnpm-installasjon, frontend-bygg og unit-tester som del av Maven-livssyklusen når tester er aktivert. Dette gjelder også når modulen tas med via `-am`.

Kjør Biome på eksplisitte filstier som er endret i oppgaven:

```bash
pnpm exec biome check frontend/src/common/dato.utils.ts
pnpm exec biome check --write frontend/src/common/dato.utils.ts
```

Bytt eksempelstien med de endrede filene. `pnpm biome` sjekker hele prosjektets konfigurerte omfang.

API-generering krever en kjørende backend, og E2E-testene krever en kjørende app. For lokal utvikling beskriver `AGENTS.md` oppstart med Docker Compose, `DevApplication.kt` og frontend. Innlogging skjer via Wonderwall på `http://localhost:4000`; Vite kjører på `http://localhost:3000`.

## CI og deployment

[build-and-test.yml](../.github/workflows/build-and-test.yml) kjører frontend-sjekker med Biome, unit- og browser-tester og typecheck. Backend bygges og kontrolleres med `mvn clean verify`, før backend-imaget bygges.

[e2e.yaml](../.github/workflows/e2e.yaml) bygger backend og mock-server, starter Docker-stacken og kjører Playwright. [main.yml](../.github/workflows/main.yml) deployer backend til dev før prod. [Pdfgen-workflowen](../.github/workflows/pdfgen.yml) har egne tester, image-bygging og deployment.

[Backend-manifestet](../.nais/app/backend.yaml) beskriver applikasjonen, PostgreSQL, Kafka, Azure/Wonderwall og integrasjonstilgang. [Pdfgen-manifestet](../.nais/app/pdfgen.yaml) beskriver den separate brevtjenesten.

## Observerte avvik og begrensninger

Konkrete avvik er samlet ved det aktuelle området: [frontend](frontend-arkitektur.md#tester-og-kjente-avvik) og [backend](backend-arkitektur.md#observerte-avvik-og-begrensninger). De er ikke en prioritert feilrapport eller et forslag om å endre kode.

Ved senere endringer må instruksjoner, domenedefinisjoner og kode vurderes hver for seg. Et eksisterende mønster er ikke i seg selv en beslutning om ønsket praksis.
