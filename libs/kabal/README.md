# Klientbibliotek mot Kabal API

Klient for å oversende klagesaker til Kabal (Klage og Anke Behandling Av ALler saker).

## Om Kabal

Kabal er NAVs system for behandling av klage- og ankesaker. Dette biblioteket gjør det mulig å oversende klagesaker fra historisk-superhelt til Kabal for videre behandling.

## API-dokumentasjon

**Dev-miljø:**
- Swagger UI: https://kabal-api.intern.dev.nav.no/swagger-ui/index.html?urls.primaryName=external
- API-endepunkt: `POST /api/oversendelse/v4/sak`

## Bruk

### Oppsett

```kotlin
@Bean
fun kabalClient(
    @Value("\${kabal.url}") kabalUrl: String,
    tokenExchangeService: TokenExchangeService
): KabalClient {
    val restClient = RestClient.builder()
        .baseUrl(kabalUrl)
        .requestInterceptor { request, body, execution ->
            request.headers.setBearerAuth(tokenExchangeService.exchangeForToken("dev-gcp:klage:kabal-api"))
            request.headers.set("X-Correlation-ID", UUID.randomUUID().toString())
            execution.execute(request, body)
        }
        .build()
    
    return KabalClient(restClient)
}
```

### Eksempel: Send klagesak til Kabal

```kotlin
val request = SendSakV4Request(
    type = SakType.KLAGE,
    sakenGjelder = Part(
        id = PartId(
            type = PartIdType.PERSON,
            verdi = "12345678910"
        )
    ),
    klager = Part(
        id = PartId(
            type = PartIdType.PERSON,
            verdi = "12345678910"
        )
    ),
    fagsak = Fagsak(
        fagsakId = "SAK123",
        fagsystem = Fagsystem.K9
    ),
    ytelse = Ytelse.OMS_OMP,
    tilknyttedeJournalposter = listOf(
        JournalpostReferanse(
            type = JournalpostType.BRUKERS_KLAGE,
            journalpostId = "830498203"
        )
    ),
    brukersKlageMottattVedtaksinstans = LocalDate.now(),
    hjemler = listOf(Hjemmel.FVL_11)
)

val response = kabalClient.sendSakV4(request)
println("Sak opprettet i Kabal med ID: ${response.behandlingId}")
```

## Modeller

### SendSakV4Request
Hovedmodellen for oversendelse av klagesaker til Kabal.

**Obligatoriske felter:**
- `type`: `KLAGE` (anke støttes ikke foreløpig)
- `sakenGjelder`: Person eller virksomhet saken gjelder
- `klager`: Person eller virksomhet som klager (kan være samme som sakenGjelder)
- `fagsak`: Referanse til fagsaken
- `ytelse`: Type ytelse (f.eks. OMS_OMP, OMS_OLP)
- `tilknyttedeJournalposter`: Liste med relevante journalposter
- `brukersKlageMottattVedtaksinstans`: Dato for når klagen ble mottatt

**Valgfrie felter:**
- `prosessfullmektig`: Prosessfullmektig i saken
- `hjemler`: Hjemler knyttet til klagen
- `kildeReferanse`: Teknisk id fra avsendersystem
- `frist`: Overstyring av frist (standard: 12 uker fra sakMottattKaTidspunkt)
- `kommentar`: Interne kommentarer
- `hindreAutomatiskSvarbrev`: Hindre automatisk utsending av svarbrev

### SendSakV4Response
Respons fra Kabal etter vellykket oversendelse.

Inneholder `behandlingId` som er unik identifikator for behandlingen i Kabal.

## Autentisering

Klienten krever:
- **Bearer token**: Må byttes mot Kabal-token via Texas/Token Exchange
- **X-Correlation-ID**: UUID for sporing av forespørsler på tvers av systemer

## Feilhåndtering

Klienten kaster `KabalClientException` ved:
- Tom respons fra API
- HTTP-feil (håndteres av Spring RestClient)

## Testing

Se `KabalClientTest.kt` for eksempler på hvordan klienten testes med mock-data.

Test-data ligger i `src/test/resources/json/`:
- `klage-request.json`: Eksempel på gyldig klagesak-request

