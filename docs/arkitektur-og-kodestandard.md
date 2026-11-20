# Arkitektur og kodestandard

Denne guiden beskriver dagens kode og den avklarte kodestandarden i Historisk Superhelt for nye og eksisterende utviklere. Den skiller mellom regler, observert praksis og avvik. Den forklarer ikke hvorfor tidligere arkitekturvalg ble gjort.

## Hvordan lese guiden

- **Regel** betyr et krav i denne guiden, prosjektets øvrige instrukser eller verktøykonfigurasjon.
- **Observert praksis** betyr et mønster i de undersøkte kodeeksemplene, ikke et krav om at all kode skal følge det.
- **Avvik** betyr en konkret forskjell mellom en regel eller domenedefinisjon og implementasjonen.

[AGENTS.md](../AGENTS.md) peker til denne guiden og inneholder øvrige prosjektinstrukser. Reglene for lagdeling, validering, transaksjonsgrenser og testdekning står her. [GLOSSARY.md](../GLOSSARY.md) definerer domenebegrepene.

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

Ordlisten skiller mellom søknad, sak, krav, behandling, vedtak og utbetaling. En søknad kan inneholde flere krav, og ett vedtak kan ha forskjellige utfall for kravene.

Dagens [Sak](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/Sak.kt) bærer gjeldende behandlingsnummer, ett vedtaksresultat og ett beløp. [Vedtak](../backend/src/main/kotlin/no/nav/historisk/superhelt/vedtak/Vedtak.kt) lagrer opplysninger knyttet til sak og behandlingsnummer. Det finnes ikke en separat backend-entitet for hvert behandlingsforløp i dagens modell.

Støtten for én utbetaling per søknad er en forenkling i dagens løsning, ikke en domeneregel. [Databasebegrensningen](../backend/src/main/resources/db/migration/V1.007__utbetaling_sak_behandlingsid_unique.sql) tillater én utbetalingsrad per sak og behandlingsnummer; samme sak kan ha utbetalingsrader fra flere behandlinger.

## En ende-til-ende-flyt

Godkjent attestering viser hvordan UI, REST, tjenester og Kafka henger sammen:

1. [AttesterSakAction](../frontend/src/routes/sak/$saksnummer/-components/AttesterSakAction.tsx) bruker React Query-mutasjonen `attersterSakMutation` fra den genererte API-pakken. Den sender godkjenning og eventuell kommentar.
2. Den [genererte SDK-en](../packages/superhelt-api/src/sdk.gen.ts) sender `PUT /api/sak/{saksnummer}/status/attester`.
3. [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) henter saken, validerer rettighet og status, og håndterer godkjenning eller retur.
4. Ved godkjenning orkestrerer controllerens `ferdigstill` brev, utbetaling, statusendring og lagring av vedtak.
5. [UtbetalingService](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingService.kt) undersøker eksisterende utbetaling og avgjør om en utbetaling skal opprettes eller gjenbrukes.
6. [UtbetalingRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingRepository.kt) håndterer persistens og mapper JPA-objekter til domenetyper.
7. [UtbetalingKafkaProducer](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingKafkaProducer.kt) sender utbetalingsmeldingen til Helved. [UtbetalingStatusConsumer](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingStatusConsumer.kt) håndterer statusmeldinger tilbake.

Rekkefølgen beskriver kallene i denne flyten. Den innebærer ikke at alle databaseendringer og eksterne kall inngår i én felles transaksjon.

### Kommunikasjonsformer

Frontend bruker REST via den genererte klienten. Backend bruker både synkrone integrasjonsklienter og Kafka. Integrasjonsbibliotekene ligger i `libs/`, mens applikasjonens integrasjonsoppsett og flyt ligger i backend.

| Kafka-topic | Bruk |
|-------------|------|
| `historisk.utbetaling.v1` | Utbetalingsmeldinger til Helved |
| `helved.status.v1` | Utbetalingsstatus fra Helved |
| `historisk.superhelt.statistikk.saksbehandling.v1` | Saksbehandlingsstatistikk |
| `klage.behandling-events.v1` | Behandlingshendelser fra Kabal |

Se [topic-manifestet](../.nais/app/topics.yaml), [backend-manifestet](../.nais/app/backend.yaml) og [prosjektoversikten](../AGENTS.md). Kabal-behandlinger er eksterne behandlingsforløp, ikke automatisk nye behandlinger i Superhelt.

## Backend

### Pakker og lag

**Regler:** Domenepakker ligger under `backend/src/main/kotlin/no/nav/historisk/superhelt/`. `db/` inneholder JPA-entities og JPA-repositories. `rest/` inneholder controllere og REST-DTO-er. JPA-entities skal ikke eksponeres utenfor repository-laget.

### Ønsket ansvarsdeling

Dette er avklart standard for backend-kode, ikke en påstand om at alle eksisterende flyter følger den.

| Lag | Ansvar |
|-----|--------|
| Controller | Håndterer HTTP, request-validering og respons. Delegerer brukstilfellet til en service |
| Service | Orkestrerer brukstilfellet og håndhever forretningsregler gjennom domenetyper eller validatorer |
| Repository | Oppslag, lagring og mapping mellom persistens- og domenetyper |

Avgjørelser om tillatte handlinger og statusoverganger hører til service-/domenelaget, ikke repositoryets hovedansvar. Databasen håndhever fortsatt sine restriksjoner.

Standarden innebærer ikke at eksisterende tilgangskontroll skal fjernes eller flyttes automatisk. Eventuell flytting av slike sjekker må vurderes separat.

### Dagens lagdeling

**Observert praksis:** [Sak](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/Sak.kt) er en `data class` med immutable felt. [SakJpaEntity](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/db/SakJpaEntity.kt) har mutable persistensfelt og en `toDomain()`-mapping. [SakRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakRepository.kt) skjuler JPA-laget for kallere og utfører oppdateringer.

Klasser bruker navn som `SakService`, `SakRepository` og `SakJpaEntity`. Funksjoner og variabler bruker camelCase; pakker bruker lowercase. Dette er observerte navnemønstre.

### DTO-er, validering og feil

**Regler:** Controlleren validerer request-format og påkrevde felt. Service-/domenelaget validerer om handlingen er tillatt ut fra sakens tilstand, også når inngangen ikke er HTTP. Domeneregler kan samles i egne validatorer som servicen bruker.

[REST-typene](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakRestTypes.kt) bruker Kotlin `data class` og Bean Validation-annotasjoner. Interne oppdateringstyper finnes også i repository- og domenelaget, for eksempel `UpdateSakDto` i [SakRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakRepository.kt).

[SakValidator](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakValidator.kt) håndterer blant annet tillatte statusoverganger. [GlobalControllerAdvice](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/GlobalControllerAdvice.kt) oversetter blant annet valideringsfeil og tilgangsnekt til HTTP-svar med `ProblemDetail`.

### Database og transaksjoner

PostgreSQL leveres gjennom Cloud SQL i [Nais-manifestet](../.nais/app/backend.yaml). [application.yaml](../backend/src/main/resources/application.yaml) leser databasekonfigurasjonen fra miljøvariabler og bruker JPA `ddl-auto: validate`. Flyway håndterer skjemaendringer med [versjonerte SQL-migrasjoner](../backend/src/main/resources/db/migration/).

**Regler:** Skjemaendringer krever en ny Flyway-migrasjon. Eksisterende migrasjoner skal ikke endres.

Vurder transaksjonsgrensen for hver operasjon. Skill lokale databaseendringer fra eksterne sideeffekter. Ikke legg som standard én `@Transactional` rundt hele serviceflyten. For operasjoner som sender brev eller betaling, må teamet ha avklart hva som skjer ved delvis feil og nye forsøk.

**Observert praksis:** Transaksjonsannotasjoner finnes på service-metoder, men også på enkelte repository- og Kafka-producer-metoder. Se [UtbetalingService](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingService.kt), [UtbetalingRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingRepository.kt) og [UtbetalingKafkaProducer](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingKafkaProducer.kt).

I [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) har tilbakestilling av gjenåpning en ytre transaksjon, mens attestering og ferdigstilling ikke har det. [BrevService](../backend/src/main/kotlin/no/nav/historisk/superhelt/brev/BrevService.kt) bruker `Propagation.NEVER` ved arkivering og distribusjon. Disse flytene kan derfor ikke samles i én transaksjon uten å vurdere dagens grenser og sideeffekter.

### Autentisering og tilgangskontroll

Dette er en beskrivelse av eksisterende kode, ikke en sikkerhetsvurdering.

[SecurityConfig](../backend/src/main/kotlin/no/nav/historisk/superhelt/config/SecurityConfig.kt) konfigurerer Spring Security som stateless JWT resource server. `/api/**` krever `READ`, `/admin/**` krever `DRIFT`, og øvrige forespørsler krever autentisering med unntak av eksplisitt åpne mønstre.

[NavJwtAuthenticationConverter](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/authentication/NavJwtAuthenticationConverter.kt) mapper gruppeclaims til roller, tilganger og tema. [Role](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/authentication/Role.kt) definerer rollenes tilganger.

Person- og tematilgang kontrolleres også ved metodekall. [SakRepository](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/SakRepository.kt) bruker blant annet `@PreAuthorize` og `@PostAuthorize`; [TilgangsmaskinAuthLogic](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/permission/TilgangsmaskinAuthLogic.kt) kobler inn Tilgangsmaskin.

Azure/Wonderwall er konfigurert i [Nais-manifestet](../.nais/app/backend.yaml). Utgående kall bruker Texas-tokenhåndtering gjennom [NaisTokenService](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/token/NaisTokenService.kt) og [request-interceptoren](../backend/src/main/kotlin/no/nav/historisk/superhelt/infrastruktur/token/NaisTokenClientRequestInterceptor.kt). Eksempler på integrasjonsoppsett er [PDL](../backend/src/main/kotlin/no/nav/historisk/superhelt/person/pdl/PdlConfig.kt) og [Kabal](../backend/src/main/kotlin/no/nav/historisk/superhelt/klage/config/KabalConfig.kt).

**Regler:** Behold nødvendige tilgangssjekker, og ikke logg fødselsnummer eller andre personopplysninger. En generell autentiseringsregel erstatter ikke personbasert tilgangskontroll.

## Frontend

### Ruter, serverdata og lokal state

**Regler:** Bruk TanStack Router for filbasert routing, TanStack Query for datahenting og genererte typer og klienter fra `@superhelt/api`. Se [AGENTS.md](../AGENTS.md).

**Observert praksis:** [Saksruten](../frontend/src/routes/sak/$saksnummer/route.tsx) bruker en loader til å fylle Query-cache og `useSuspenseQuery` til å lese serverdata. [SakOpplysningerEditor](../frontend/src/routes/sak/$saksnummer/-components/SakOpplysningerEditor.tsx) bruker debouncing og cacheoppdatering ved redigering.

[AttesterSakAction](../frontend/src/routes/sak/$saksnummer/-components/AttesterSakAction.tsx) viser skillet mellom lokal inputstate med `useState` og serverendringer med `useMutation`. Komponenter bruker PascalCase, funksjoner og variabler camelCase, og props beskrives med TypeScript-typer.

### API-generering

[openapi-ts.config.ts](../packages/superhelt-api/openapi-ts.config.ts) genererer typer, SDK og React Query-hjelpere fra backendens `/v3/api-docs`. Generering krever at backend kjører. Genererte filer oppdateres gjennom generatoren, ikke som vanlige håndskrevne klienter.

Den faktiske package-exporten for React Query-hjelperne er `@superhelt/api/react-query.gen`. Se [API-pakkens package.json](../packages/superhelt-api/package.json) og importene i `AttesterSakAction`.

### UI og feilhåndtering

**Regler:** Bruk Nav Aksel. Spacing skal bruke `Box` og `space-*`-tokens, ikke Tailwind margin- og padding-klasser.

**Observert praksis:** [Saksruten](../frontend/src/routes/sak/$saksnummer/route.tsx) bruker Aksel-komponenter og spacing-tokens. [ErrorAlert](../frontend/src/common/error/ErrorAlert.tsx) viser API-feil, og ruten har en egen feilkomponent.

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

### Dagens testeksempler

| Testområde | Representativt eksempel |
|------------|-------------------------|
| Backend-repository | [SakRepositoryTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/SakRepositoryTest.kt) |
| Backend-controller | [SakActionControllerAttesterTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionControllerAttesterTest.kt) |
| Kafka-integrasjon | [UtbetalingKafkaProducerIntegrationTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/utbetaling/kafka/UtbetalingKafkaProducerIntegrationTest.kt) |
| Integrasjonsklient | [DokarkivClientTest](../libs/dokarkiv/src/test/kotlin/no/nav/dokarkiv/DokarkivClientTest.kt) |
| React-komponent | [ProcessMenu.test.tsx](../frontend/src/common/process-menu/ProcessMenu.test.tsx), med Vitest og Testing Library |
| Brukerflyt | [henlegg.spec.ts](../e2e/end-to-end/henlegg.spec.ts), med Playwright |

De undersøkte attesteringstestene kaller controller-metoder direkte. De er derfor ikke i seg selv tester av HTTP-validering og respons. Se [SakActionControllerAttesterTest](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionControllerAttesterTest.kt) og [testoppsettet](../backend/src/test/kotlin/no/nav/historisk/superhelt/sak/rest/AbstractSakActionTest.kt).

### Hva verktøyene håndhever

[Biome-konfigurasjonen](../biome.json) angir blant annet fire mellomrom, linjebredde 120 og anbefalte lint-regler. [Frontendens Biome-konfigurasjon](../frontend/biome.json) arver root-oppsettet og ekskluderer genererte `.gen.ts`-filer.

**Regel:** Kjør Biome bare på filer som er endret i oppgaven. Root-scriptet `pnpm biome` dekker hele konfigurasjonens omfang; det er ikke en avgrenset sjekk av endrede filer.

I de undersøkte Maven- og CI-konfigurasjonene er det ikke funnet Kotlin-formattering med Spotless/ktlint, detekt eller arkitekturtester. Kotlin-navnemønstrene ovenfor skal derfor ikke leses som regler håndhevet av en slik sjekk.

### Vanlige kommandoer

Kommandoene finnes i [package.json](../package.json), [frontend/package.json](../frontend/package.json) og [prosjektinstruksene](../AGENTS.md).

```bash
mvn test
mvn test -pl backend -am -Dtest=SakValidatorTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm test
pnpm typecheck
pnpm frontend:build
pnpm api:generate
pnpm e2e:test
```

Backend-tester krever Docker. API-generering krever en kjørende backend, og E2E-testene krever en kjørende app. For lokal utvikling beskriver `AGENTS.md` oppstart med Docker Compose, `DevApplication.kt` og frontend. Innlogging skjer via Wonderwall på `http://localhost:4000`; Vite kjører på `http://localhost:3000`.

## CI og deployment

[build-and-test.yml](../.github/workflows/build-and-test.yml) kjører frontend-sjekker med Biome, unit- og browser-tester og typecheck. Backend bygges og kontrolleres med `mvn clean verify`, før backend-imaget bygges.

[e2e.yaml](../.github/workflows/e2e.yaml) bygger backend og mock-server, starter Docker-stacken og kjører Playwright. [main.yml](../.github/workflows/main.yml) deployer backend til dev før prod. [Pdfgen-workflowen](../.github/workflows/pdfgen.yml) har egne tester, image-bygging og deployment.

[Backend-manifestet](../.nais/app/backend.yaml) beskriver applikasjonen, PostgreSQL, Kafka, Azure/Wonderwall og integrasjonstilgang. [Pdfgen-manifestet](../.nais/app/pdfgen.yaml) beskriver den separate brevtjenesten.

## Observerte avvik og begrensninger

Tabellen beskriver konkrete forskjeller. Den er ikke en prioritert feilrapport eller et forslag om å endre kode.

| Tema | Regel eller domenedefinisjon | Observert implementasjon |
|------|-----------------------------|--------------------------|
| React Query-import | `AGENTS.md` oppgir `@superhelt/api/react-query` | [Package-exporten](../packages/superhelt-api/package.json) og komponentene bruker `@superhelt/api/react-query.gen` |
| Forretningslogikk | Services skal orkestrere brukstilfellene og håndheve domeneregler | [SakActionController](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) validerer og orkestrerer også saksflyt |
| Transaksjonsgrenser | Grensen skal vurderes per operasjon, med avklart håndtering av sideeffekter | Controllerens `ferdigstill` er ikke annotert; underliggende metoder har egne annotasjoner. Du kan ikke anta én samlet transaksjon i denne flyten |
| Spacing | Prosjektet krever Aksel `Box` og spacing-tokens | [Saksruten](../frontend/src/routes/sak/$saksnummer/route.tsx) inneholder også inline `marginTop` |
| Utbetalingsstatus | Instruksens oversikt bruker `SENDT`, `MOTTATT` og `BEHANDLET` | [Enumen](../backend/src/main/kotlin/no/nav/historisk/superhelt/utbetaling/UtbetalingStatus.kt) bruker `SENDT_TIL_UTBETALING`, `MOTTATT_AV_UTBETALING` og `BEHANDLET_AV_UTBETALING` |
| Henlegging | Ordlisten definerer henlegging som vedtak | [Henleggingsflyten](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/rest/SakActionController.kt) setter `FERDIG` og `HENLAGT`, men kaller ikke vedtakslagringen |
| Gjenåpning og revurdering | Ordlisten skiller oppstart av ny behandling fra behandlingens formål | [StatistikkService](../backend/src/main/kotlin/no/nav/historisk/superhelt/statistikk/StatistikkService.kt) klassifiserer gjenåpning som `REVURDERING` |
| Flere krav | Søknader og vedtak kan omfatte flere krav med ulike utfall | Dagens [Sak](../backend/src/main/kotlin/no/nav/historisk/superhelt/sak/Sak.kt) og [Vedtak](../backend/src/main/kotlin/no/nav/historisk/superhelt/vedtak/Vedtak.kt) bruker ett resultat og ett beløp |

Ved senere endringer må instruksjoner, domenedefinisjoner og kode vurderes hver for seg. Et eksisterende mønster er ikke i seg selv en beslutning om ønsket praksis.
