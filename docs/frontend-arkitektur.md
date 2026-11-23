# Frontend-arkitektur

Frontenden er en React-applikasjon med TanStack Router, TanStack Query og Nav Aksel. Denne beskrivelsen bygger på kodegjennomgang, ikke på utprøving i nettleseren. Felles kodestandard og systemets ende-til-ende-flyt står i [arkitektur og kodestandard](arkitektur-og-kodestandard.md); domenebegrepene står i [ordlisten](../GLOSSARY.md).

## Oppstart og ansvarsgrenser

[main.tsx](../frontend/src/main.tsx) oppretter routeren med den delte `queryClient` fra API-pakken. `QueryClientProvider` omgir `RouterProvider`, og router-konteksten gir loadere tilgang til samme cache som komponentene. Oppstarten importerer også Aksel CSS og initialiserer Faro.

[Rotruten](../frontend/src/routes/__root.tsx) eier den felles siderammen med header, footer og Aksels lyse tema. Den henter innlogget brukers roller og viser en advarsel fremfor underliggende ruter når brukeren mangler saksbehandlingsroller. Dette er visningslogikk; backend håndhever tilgang til data og handlinger.

| Del | Ansvar |
|-----|--------|
| `frontend/src/routes/` | Filbaserte ruter, route-loadere og rutespesifikke komponenter og API-hjelpere |
| `frontend/src/common/` | Delte komponenter, query-hjelpere og funksjoner |
| `packages/superhelt-api/` | Genererte API-typer, SDK og Query-hjelpere, samt håndskrevet felles klientoppsett |
| `packages/breveditor/` | Delt HTML-editor med Tiptap og PDF-forhåndsvisning |

Dette er ansvarsområder i dagens kode, ikke uavhengig deploybare applikasjoner. Frontenden pakkes og deployes sammen med backend.

## Routing og innlasting

[Vite-oppsettet](../frontend/vite.config.ts) kjører TanStack Router-pluginen før React-pluginen. Pluginen genererer `routeTree.gen.ts` og deler opp rutekoden automatisk. Aliaset `~/` peker til `frontend/src/`. Mapper som `-components/` og `-api/` samler hjelpefiler ved rutene.

Routeren preloader ved brukerintensjon, for eksempel når en lenke pekes på, og har `defaultPreloadStaleTime: 0`. Query-cachen brukes også ved slik innlasting.

[Saksruten](../frontend/src/routes/sak/$saksnummer/route.tsx) viser to deler av innlastingen:

1. Loaderen starter `ensureQueryData` for saken og sakens journalposter. Den verken avventer eller returnerer disse løftene.
2. `SakLayout` leser dataene med `useSuspenseQuery` og henter også person og brukerens journalposter. Komponentens lesing kan dermed suspendere mens data fortsatt lastes.

Dette er ikke et generelt mønster der alle loadere venter på alle data. [Sakens indeksrute](../frontend/src/routes/sak/$saksnummer/index.tsx) avventer saken før den velger hvilken underside brukeren skal sendes til.

## API-kontrakt og serverdata

[API-generatoren](../packages/superhelt-api/openapi-ts.config.ts) leser backendens `/v3/api-docs` og genererer fetch-klient, typer, SDK og TanStack Query-hjelpere. Backend må kjøre lokalt på port 8080 når `pnpm api:generate` kjøres.

[Pakkens exports](../packages/superhelt-api/package.json) skiller mellom:

- `@superhelt/api` for typer og SDK.
- `@superhelt/api/react-query.gen` for genererte query options, query keys og mutasjoner.
- `@superhelt/api/query-client` for det håndskrevne felles klientoppsettet.

De genererte TypeScript-typene beskriver kontrakten ved kompilering. Generatoroppsettet konfigurerer ikke en runtime-validator for responsskjemaet.

Håndskrevne query-hjelpere tilpasser de genererte funksjonene til skjermbildene. [useInvalidateSakQuery](../frontend/src/routes/sak/$saksnummer/-api/useInvalidateSakQuery.ts) invaliderer saken, endringsloggen og sakens journalposter etter endringer. Invalidasjonene startes uten at helperen avventer refetch.

## Redigering og cacheoppdatering

Serverdata ligger i Query-cachen. Skjemaene har i tillegg lokal inputstate mens brukeren redigerer. To representative varianter har ulike lagringsforløp:

| Skjermdel | Lagring og tilbakemelding |
|-----------|--------------------------|
| [SakOpplysningerEditor](../frontend/src/routes/sak/$saksnummer/-components/SakOpplysningerEditor.tsx) | Debouncer endringer i to sekunder før automatisk lagring. Ved suksess oppdateres saken i cachen. «Lagre og gå videre» avventer lagring før validering og navigasjon |
| [BrevEditor](../frontend/src/routes/sak/$saksnummer/-components/BrevEditor.tsx) | Bruker `useAutosave` med 500 ms debounce. Mutasjonen oppdaterer brevets cache, invaliderer brev-queryen og oppdaterer synlig lagringsstatus. Feil gir feilstatus og markerer innholdet som endret igjen |

Automatisk lagring og eksplisitt innsending er altså forskjellige kodeveier. At en mutasjon er ferdig, betyr heller ikke at alle invaliderte queries er ferdig lastet.

## Attestering fra brukergrensesnittet

[Oppsummeringsruten](../frontend/src/routes/sak/$saksnummer/oppsummering.tsx) viser attesteringshandlingen når saken er `TIL_ATTESTERING`.

[AttesterSakAction](../frontend/src/routes/sak/$saksnummer/-components/AttesterSakAction.tsx) bruker lokal state for avgjørelse og kommentar, og den genererte `attersterSakMutation` for serverkallet. Navnet følger backendens eksisterende OpenAPI-operation-ID.

1. Brukeren velger godkjenning eller retur. Ved retur krever UI-et en trimmet kommentar på mer enn fem tegn.
2. Mutasjonen sender avgjørelsen til backend. Knappen viser pending-status, og mutasjonsfeil vises med `ErrorAlert`.
3. Ved godkjenning starter komponenten invalidasjon av saksdata, slik at skjermbildet kan oppdateres fra serverens nye tilstand.
4. Ved retur åpner komponenten en forklarende dialog. Denne suksessgrenen kaller ikke samme invalidasjonshelper.

Backendens kontroller og eksterne sideeffekter beskrives i [backend-arkitekturen](backend-arkitektur.md#attestering-og-ferdigstilling).

## Feil og validering

[queryClient.ts](../packages/superhelt-api/src/queryClient.ts) oversetter HTTP-feil med tilgjengelig respons til `ApiError`, med faktisk HTTP-status og problemfelter. Query-policyen stopper retries for 4xx unntatt 429; øvrige query-feil forsøkes på nytt mens `failureCount < 3`. Dette er query-policy, ikke en generell retry-garanti for mutasjoner.

[ErrorAlert](../frontend/src/common/error/ErrorAlert.tsx) viser problemets tittel og detalj når feltene finnes. Ellers bruker den en generell tittel og tilgjengelig feilmelding. [RfcErrorBoundary](../frontend/src/common/error/RfcErrorBoundary.tsx) fanger renderfeil; rutene har også egne `errorComponent`-er. Mutasjonsfeil håndteres eksplisitt av komponentene som starter handlingene.

Valideringsfeil er ikke bare HTTP-feil. Saks- og brevmodellene kan inneholde `valideringsfeil` med `field` og `message`. Editorene bruker disse til feltfeil og Aksel `ErrorSummary`.

## Breveditor og PDF-grensen

[HtmlPdfgenEditor](../packages/breveditor/src/HtmlPdfgenEditor.tsx) deler HTML-en i fast innhold og et redigerbart område merket `.data-editable`. [TiptapEditor](../packages/breveditor/src/TiptapEditor.tsx) redigerer dette området og sender HTML tilbake gjennom `onChange`. Editorutvidelsene omfatter blant annet standardtekster og utheving.

`BrevEditor` henter brevmodellen og HTML fra backend, lagrer redigert innhold via API-et og bruker backendens PDF-endpoint til forhåndsvisning. Nettleseren kaller ikke pdfgen direkte. Backend håndterer overgangen til den separate Typst-baserte tjenesten.

## Tester og kjente avvik

[Vitest-oppsettet](../vitest.shared.ts) skiller unit-tester fra `*.browser.test.ts(x)`, som kjøres i headless Chromium. [Frontendens oppsett](../frontend/vitest.config.ts) bruker jsdom for unit-prosjektet. Breveditorens browser-tester dekker komponenter i en faktisk nettleser, mens [Playwright E2E](../e2e/playwright.config.ts) kjører mot appen gjennom Wonderwall.

[Attesteringsflyten i E2E](../e2e/end-to-end/saksbehandling-attestert.spec.ts) er et eksempel på godkjent saksflyt. Den dokumenterer ikke alene hvordan alle feil- og gjenopprettingssituasjoner virker.

| Tema | Avklart regel | Observert avvik |
|------|---------------|-----------------|
| API-import | Bruk pakkens eksporterte Query-hjelpere | Rotens `AGENTS.md` oppgir `@superhelt/api/react-query`, mens pakken og komponentene bruker `@superhelt/api/react-query.gen` |
| Spacing | Bruk Aksel `Box` og spacing-tokens | Saksrutens `Tabs` bruker også inline `marginTop` |

Se [felles teststandard](arkitektur-og-kodestandard.md#ønsket-testdekning) for valg av testnivå og [Copilot-instruksene](../.github/copilot-instructions.md#bygg-tester-og-lint) for kommandoer.
