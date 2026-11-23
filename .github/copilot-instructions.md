# Historisk Superhelt

Superhelt behandler engangsutbetalinger for enkle helseytelser og erstatter InfoTrygd-rutinene HT-MV, SB-SA og GE-PP.

## Les før arbeidet

- Følg `AGENTS.md` i roten og eventuelle lokale `AGENTS.md` i området du endrer.
- Før utforsking: les `docs/agents/domain.md` og `GLOSSARY.md`, samt relevante ADR-er i `docs/adr/` hvis de finnes. Bruk ordlistens skille mellom sak, behandling, krav, vedtak og utbetaling.
- Før endringer i backend-kode eller tester: les `docs/arkitektur-og-kodestandard.md`. Guiden skiller avklart standard fra dagens praksis og dokumenterer kjente avvik.
- Før frontend-endringer: les `docs/frontend-arkitektur.md` om routing, cache og redigering. Før backend-endringer: les `docs/backend-arkitektur.md` om domenemodell, transaksjoner og integrasjoner.
- Før arbeid med issues: les `docs/agents/issue-tracker.md`. Før triage eller endring av triage-etiketter: les `docs/agents/triage-labels.md`.
- For React-komponenter gjelder også `.github/instructions/react.instructions.md`. For brev og PDF-er: les `pdfgen/AGENTS.md`.

## Bygg, tester og lint

Kjør kommandoene fra repo-roten. Prosjektet krever Java 25 og Node.js 26. `package.json` velger pnpm-versjonen; installer workspace-avhengigheter med `pnpm install --frozen-lockfile`.

| Oppgave | Kommando |
|---|---|
| Bygg og verifiser Maven-modulene, inkludert frontend | `mvn clean verify` |
| Bygg uten tester | `mvn package -DskipTests` |
| Kjør Maven-tester | `mvn test` |
| Én backend-testklasse med nødvendige moduler | `mvn test -pl backend -am -Dtest=SakValidatorTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Unit-tester i pnpm-workspaces | `pnpm test` |
| Én frontend-testfil | `pnpm --filter super-frontend test src/common/dato.utils.test.ts` |
| Typekontroll i workspaces | `pnpm typecheck` |
| Produksjonsbygg av frontend | `pnpm frontend:build` |
| Browser-tester | `pnpm test:browser` |
| Én browser-testfil | `pnpm --filter @superhelt/breveditor test:browser src/HtmlPdfgenEditor.browser.test.tsx` |
| Alle E2E-tester | `pnpm e2e:test` |
| Én E2E-testfil | `pnpm e2e:test end-to-end/henlegg.spec.ts` |
| Pdfgen unit-/integrasjonstester | `pnpm --dir pdfgen test` / `pnpm --dir pdfgen test:integration` |

Vitest skiller `*.test.ts(x)` fra `*.browser.test.ts(x)` og `*.integration.test.ts(x)`. `pnpm test` inkluderer ikke browser- eller integrasjonstestene. Browser-tester krever Chromium, som installeres med `pnpm test:browser:install`.

Kjør Biome bare på filer som er endret i oppgaven:

```bash
pnpm exec biome check frontend/src/common/dato.utils.ts
pnpm exec biome check --write frontend/src/common/dato.utils.ts
```

Bytt eksempelstien med de endrede filene. `pnpm biome` sjekker hele prosjektets konfigurerte omfang.

Backend-integrasjonstester bruker Testcontainers og krever Docker. Maven-modulen `frontend` kjører pnpm-installasjon, frontend-bygg og unit-tester som del av Maven-livssyklusen, også når den tas med via `-am`.

### Lokal app og API-generering

1. Start avhengigheter med `docker compose up`.
2. Kjør `backend/src/test/kotlin/no/nav/historisk/superhelt/DevApplication.kt` med `dev`-profilen.
3. Start Vite med `pnpm frontend:start`.
4. Logg inn via `http://localhost:4000` (Wonderwall). Vite på `http://localhost:3000` kan brukes etter innlogging og proxyer `/api` og `/oauth2` til Wonderwall.

E2E-testene starter ikke appen. De bruker `http://localhost:4000` som standard; `BASE_URL` overstyrer adressen. Gjenbruk fixtures fra `e2e/end-to-end/test.fixtures.ts` og eksisterende page objects.

Etter endringer i REST-kontrakten: kjør `pnpm api:generate` med backend tilgjengelig på `http://localhost:8080/v3/api-docs`.

## Arkitektur og saksflyt

Repoet har to bygginndelinger: Maven for backend, integrasjonsbiblioteker, mocks og frontend-pakking; pnpm-workspaces for React, delte TypeScript-pakker, E2E og pdfgen-tester. Frontenden pakkes som statiske ressurser i en Maven-JAR som backend avhenger av. Spring Boot serverer dermed UI og API fra samme deploybare image.

Backend er en Kotlin/Spring Boot-applikasjon med PostgreSQL/JPA og Kafka. Domenepakkene ligger under `backend/src/main/kotlin/no/nav/historisk/superhelt/`. Integrasjonsklienter og felles verdityper ligger i `libs/`; backend konfigurerer klientene og orkestrerer saksflyten.

Saksbehandler oppretter og fyller ut en sak, så attesterer en attestant. Ferdigstilling sender brev til journalføring og distribusjon, sender eventuell utbetaling til Helved og lagrer vedtak. `SakActionController` er et sentralt inngangspunkt i dagens implementasjon. Flyten omfatter eksterne sideeffekter og er ikke én samlet databasetransaksjon.

| Kafka-topic | Flyt |
|---|---|
| `historisk.utbetaling.v1` | Utbetalinger til Helved |
| `helved.status.v1` | Utbetalingsstatus fra Helved |
| `historisk.superhelt.statistikk.saksbehandling.v1` | Saksbehandlingsstatistikk ut |
| `klage.behandling-events.v1` | Behandlingshendelser fra Kabal inn |

`packages/breveditor` er en delt Tiptap-basert editor. `pdfgen/` er en separat Typst-basert brevtjeneste kjørt med `pdfgenrs`, med eget image og deployment. Backend bruker klienten i `libs/pdfgen` for å generere PDF-er.

Deployment og integrasjonstilgang ligger i `.nais/`. Felles bygg og sjekker ligger i `.github/workflows/build-and-test.yml`; main deployer backend til dev før prod. Pdfgen har egen workflow.

## Konvensjoner som påvirker endringer

- Skill domenemodell fra persistens. `db/` inneholder JPA-entities og Spring Data-repositories; repository-laget mapper til domenetyper. `rest/` inneholder controllere og REST-DTO-er. Bruk felles verdityper som `Saksnummer`, `FolkeregisterIdent` og `Belop` fra `libs/common-types`.
- Følg løkarkitekturen og ansvarsdelingen i arkitekturguiden: domenet er kjernen, services orkestrerer brukstilfeller, og ytre adaptere håndterer HTTP og persistens. Guiden avklarer unntaket for enkle serialiseringsannotasjoner. Eksisterende controllerlogikk er ikke automatisk mønsteret for ny kode.
- Vurder transaksjonsgrenser mot eksterne sideeffekter og nye forsøk. Brevsending og betaling kan ikke uten videre legges i én ytre `@Transactional`.
- Behold metodebasert tilgangskontroll, inkludert `@PreAuthorize`, `@PostAuthorize` og person-/temasjekker via Tilgangsmaskin. Azure AD/Wonderwall håndterer innlogging; Texas håndterer utgående token exchange. Ikke logg fødselsnummer eller andre personopplysninger.
- Legg skjemaendringer i nye Flyway-migrasjoner under `backend/src/main/resources/db/migration/`. Behold eksisterende migrasjoner uendret; JPA bruker `ddl-auto: validate`.
- REST-feil håndteres som Spring `ProblemDetail` via `GlobalControllerAdvice`. Frontendens delte `queryClient` i `@superhelt/api/query-client` oversetter API-feil og styrer retries.
- Bruk genererte typer og SDK fra `@superhelt/api`. Den faktiske exporten for React Query-hjelpere er `@superhelt/api/react-query.gen`, selv om rotens `AGENTS.md` oppgir `@superhelt/api/react-query`. Oppdater generert klientkode med generatoren.
- Bruk TanStack Routers filbaserte ruter. Rutespesifikke hjelpefiler ligger blant annet i `-components/` og `-api/`; `frontend/src/routeTree.gen.ts` genereres av router-pluginen. Aliaset `~/` peker til `frontend/src/`.
- Følg eksisterende Query-mønster: delte query options, prefetch med `ensureQueryData` i route-loadere og `useSuspenseQuery` i komponenter. Bruk mutasjoner og Query-cache for serverendringer.
- Bruk Nav Aksel og `Box` med `space-*`-tokens for spacing fremfor Tailwind margin-/padding-klasser.
- Kotlin-tester bruker JUnit 5 og AssertJ. Velg testnivå etter risikoen; ikke gjenta samme atferd på flere nivåer uten en egen grunn. Noen controller-tester kaller metoder direkte og verifiserer derfor ikke HTTP-validering eller sikkerhetsfiltre.
- For den agentgenererte PR-oppsummeringen: rediger `.github/workflows/pr-summary.md`, kjør `gh aw compile pr-summary --strict`, og versjoner kilden og genererte filer sammen. `pr-summary.lock.yml` genereres, ikke håndredigeres. Detaljer om samordnet gh-aw-oppgradering står i `README.md`.
