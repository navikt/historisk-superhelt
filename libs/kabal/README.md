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
