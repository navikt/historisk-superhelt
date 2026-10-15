# Pdfgen

## Lokal utvikling

Pdfgen kjører i Docker på `http://localhost:8086`.
Kontroller at tjenesten svarer før integrasjonstestene kjøres.
Endringer i fonter og maler må være synkronisert til containeren før PDF-er verifiseres.

Sjekk at tjenesten kan generere PDF med eksempeldataene (`DEV_MODE=true`):

```bash
curl --fail --silent --show-error --output /dev/null \
  http://localhost:8086/api/v1/genpdf/superhelt/brev
```

Kallet lykkes når PDF-en er generert. `--fail` gir feilkode ved HTTP-feil.

## Tester

Kjør fra repository-roten:

```bash
pnpm --dir pdfgen test
pnpm --dir pdfgen test:integration
```

Integrasjonstestene bruker port 8086 som standard. `PDFGEN_URL` kan overstyre adressen.

## Fonter

Bruk Source Sans 3 i tråd med Aksels brevstandard.
Fontfilene i `fonts/` kommer fra Adobe Source Sans, release `3.052R`.
Behold normal, fet, kursiv og fet kursiv samt den medfølgende fontlisensen.
