# Klientbibliotek mot Kabal API

Klient for å oversende klagesaker til Kabal (Klage og Anke Behandling Av Alle saker).

## API-dokumentasjon

**Dev-miljø:**
- Swagger UI: https://kabal-api.intern.dev.nav.no/swagger-ui/index.html?urls.primaryName=external
- API-endepunkt: `POST /api/oversendelse/v4/sak`

## Autentisering

Klienten krever:
- **Bearer token**: Må byttes mot Kabal-token via Texas/Token Exchange
- **X-Correlation-ID**: UUID for sporing av forespørsler på tvers av systemer

## Feilhåndtering

Klienten kaster `IllegalStateException` ved tom respons fra API eller HTTP-feil.

## Datamodell

### Sending — `SendSakV4Request`

Sendes via `KabalClient.sendSakV4(request)`. Kabal returnerer ingen body — suksess innebærer ingen exception.

Påkrevde felt: `type`, `sakenGjelder`, `klager`, `fagsak`, `kildeReferanse`, `hjemler`, `forrigeBehandlendeEnhet`, `tilknyttedeJournalposter`, `ytelse`.

```kotlin
SendSakV4Request(
    type = SakType.KLAGE,           // KLAGE eller ANKE
    sakenGjelder = SakenGjelder(Ident(IdentType.PERSON, fnr)),
    klager = Klager(Ident(IdentType.PERSON, fnr)),
    fagsak = Fagsak(fagsakId = saksnummer, fagsystem = "SUPERHELT"),
    kildeReferanse = saksnummer,    // brukes av Kabal ved tilbakemelding
    hjemler = listOf("FTRL_10_7I"), // se Hjemmel.kt for alle verdier
    forrigeBehandlendeEnhet = navEnhet,
    ytelse = KabalYtelse.HEL_HEL,
    tilknyttedeJournalposter = emptyList(),
)
```

### Støttede ytelser — `KabalYtelse`

| Verdi | Beskrivelse |
|---|---|
| `HEL_HEL` | Helsetjenester |
| `HJE_HJE` | Hjelpemidler |
| `HJE_AUR` | Arbeid og utdanningsreiser |

### Hjemler — `Hjemmel`

`Hjemmel.forYtelse(kabalYtelse)` returnerer gyldige hjemler for en gitt ytelse. Se `Hjemmel.kt` for komplett liste (FTRL, FS_ORT_HJE_MM, FS_HJE_MM, FVL, TRRL m.fl.).

## Kafka — mottatte events

Kabal sender behandlingsstatus tilbake på topic `klage.behandling-events.v1` som `BehandlingEvent`.

Feltet `kildeReferanse` matcher `kildeReferanse` i den opprinnelige `SendSakV4Request`.

### `BehandlingEventType`

| Type | Beskrivelse |
|---|---|
| `KLAGEBEHANDLING_AVSLUTTET` | Klage ferdigbehandlet |
| `ANKEBEHANDLING_OPPRETTET` | Anke mottatt i Kabal |
| `ANKEBEHANDLING_AVSLUTTET` | Anke ferdigbehandlet |
| `ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET` | Anke sendt til Trygderetten |
| `BEHANDLING_FEILREGISTRERT` | Sak feilregistrert i Kabal |
| `BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET` | Opphevet etter Trygderetten |
| `OMGJOERINGSKRAVBEHANDLING_AVSLUTTET` | Omgjøringskrav avsluttet |
| `GJENOPPTAKSBEHANDLING_AVSLUTTET` | Gjenopptak avsluttet |

Utfall for klage (`KlageUtfall`): `TRUKKET`, `RETUR`, `OPPHEVET`, `MEDHOLD`, `DELVIS_MEDHOLD`, `STADFESTELSE`, `UGUNST`, `AVVIST`, `HENLAGT`.

