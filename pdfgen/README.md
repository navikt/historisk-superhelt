# pdfgen


Maler og innhold for generering av brev for superhelt

# Komme i gang


## Start utviklingsmiljø på port 8086
```bash
./run_development.sh
```

# Api

## Superhelt
http://localhost:8086/api/v1/genpdf/superhelt/brev

``` typescript

interface BrevRequest {
   behandlingsnummer: string
   personalia: Personalia
   datoForUtsending: string
   saksbehandlerNavn: string
   beslutterNavn: string
   kontor: string
   html: string
   mottaker?: "BRUKER" | "SAMHANDLER"
   brevtype?: "VEDTAKSBREV" | "INNHENTINGSBREV" | "INFORMASJONSBREV" | "BREV"
}

interface Personalia {
   ident: string
   fornavn: string
   etternavn: string
}

```

se [json eksempel superhelt](data/superhelt/brev.json)


