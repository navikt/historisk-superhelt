# pdfgen

Typst-maler for brev i Superhelt, kjørt med [pdfgenrs](https://github.com/navikt/pdfgenrs).
Dockerfile bruker en fast release fra [pdfgenrs-releases](https://github.com/navikt/pdfgenrs/releases).
pdfgenrs laster API-malene `superhelt/brev` og `nav-brev/brev` fra `templates/`.
Superhelt importerer felles `.typ`-hjelpmaler fra `resources/lib/`.
Gamle HBS-maler, den ubrukte endringer-malen og Helsetjenester-inngangen ligger
i `til_sletting/` for senere sletting. Denne mappen kopieres ikke inn i imaget.

**Ikke merge til main eller deploy før klienten er migrert til `innhold_typst`.**
Workflowen deployer automatisk fra main. Backend og editor bruker fortsatt den gamle HTML-kontrakten.

## Lokal utvikling

Kjør fra repository-roten:

```bash
./pdfgen/run_development.sh
```

Tjenesten kjører på port 8086. `DEV_MODE=true` aktiverer GET-endpointet med
[eksempeldata](data/superhelt/brev.json):

http://localhost:8086/api/v1/genpdf/superhelt/brev

pdfgenrs laster maler, fonter og eksempeldata ved oppstart. Restart etter endringer:

```bash
docker compose -f pdfgen/compose.yml restart pdfgen
```

`DEV_MODE` skal ikke aktiveres i produksjon.

## Brevdata og API

`superhelt/brev` bruker kontrakten under.
`nav-brev/brev` er en selvstendig mal med [egne eksempeldata](data/nav-brev/brev.json).
Send JSON med POST til `/api/v1/genpdf/{malnavn}` for å få en PDF:

```bash
curl --fail-with-body \
  -H "Content-Type: application/json" \
  --data-binary @pdfgen/data/superhelt/brev.json \
  http://localhost:8086/api/v1/genpdf/superhelt/brev \
  --output brev.pdf
```

```typescript
interface BrevRequest {
    behandlingsnummer: string;
    personalia: Personalia;
    datoForUtsending: string;
    saksbehandlerNavn: string;
    beslutterNavn?: string;
    kontor: string;
    innhold_typst: string;
    mottaker?: "BRUKER" | "SAMHANDLER";
    brevtype?: "VEDTAKSBREV" | "INNHENTINGSBREV" | "INFORMASJONSBREV" | "BREV";
    målform?: "NB" | "NN";
    forhandsvisning?: boolean;
}

interface Personalia {
    ident: string;
    fornavn: string;
    etternavn: string;
}
```

`innhold_typst` er påkrevd og renderes som Typst-markup. Eksempel:

```typst
= Vedtak

Dette er et avsnitt med *fet* og _kursiv_ tekst.\
Dette er en ny linje.

- Første punkt
  + Nestet punkt
- Andre punkt

#link("https://www.nav.no")[Les mer på nav.no]
```

Malene evaluerer bare `innhold_typst`. Øvrige datafelt settes inn som tekst.
Markup-modus er ikke en sikkerhetsgrense og tillater Typst-uttrykk.
Klienten må derfor sende kontrollert innhold og escape fritekst ved konvertering.
Manglende felt, feil datatype eller ugyldig Typst gir en renderingsfeil, ikke HTML-fallback.
Ikke send reelle personopplysninger ved lokal testing.

Brukerbrev får standardtekster på valgt målform. Klage- og innsynstekst inkluderes
bare i vedtaksbrev til brukere. Samhandlerbrev får ikke disse standardtekstene.

## Drift

Health checks ligger på `/internal/is_alive` og `/internal/is_ready`.
Prometheus-metrikker ligger på `/internal/metrics`. Nais bruker SDK-autoinstrumentering.
