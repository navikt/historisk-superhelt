# pdfgen

Typst-maler for brev i Superhelt, kjørt med [pdfgenrs](https://github.com/navikt/pdfgenrs).
Dockerfile bruker en fast release fra [pdfgenrs-releases](https://github.com/navikt/pdfgenrs/releases).
pdfgenrs laster API-malen `superhelt/brev` fra `templates/`.

## Lokal utvikling

Kjør fra repository-roten:

```bash
docker compose up --watch --build
```

Tjenesten kjører på port 8086. `DEV_MODE=true` aktiverer GET-endpointet med
[eksempeldata](data/superhelt/brev.json):

http://localhost:8086/api/v1/genpdf/superhelt/brev
http://localhost:8086/api/v1/genhtml/superhelt/brev


## Brevdata og API

`superhelt/brev` bruker kontrakten under.
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

