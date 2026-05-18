# Mock-server

Mock-serveren tilbyr forhåndsdefinerte testpersoner og simulerte tjenestesvar for lokal utvikling.

## Testpersoner

### Vanlige personer
Alle 11-sifrede FNR som ikke er listet nedenfor genererer en tilfeldig person.

### Personer avvist av Tilgangsmaskin
- `40400000000` — Ukjent person (UKJENT_PERSON)
- `40300000001` — Avvist på grunn av habilitet (AVVIST_HABILITET)
- `40300000002` — Avvist på grunn av dødsdato (AVVIST_AVDØD)
- `40300000006` — Avvist på grunn av strengt fortrolig adresse (AVVIST_STRENGT_FORTROLIG_ADRESSE)
- `40300000007` — Avvist på grunn av fortrolig adresse (AVVIST_FORTROLIG_ADRESSE)

### Personer med adressebeskyttelse (godkjent av Tilgangsmaskin)
- `60000000001` — Strengt fortrolig adresse (STRENGT_FORTROLIG)
- `60000000002` — Fortrolig adresse (FORTROLIG)
- `60000000003` — Strengt fortrolig adresse utland (STRENGT_FORTROLIG_UTLAND)

### Person med dødsdato
- `70000000001` — Person med dødsdato: 2023-06-15

### Personer med verge eller fullmektig
- `7000000002` — Person med verge med tjenestevirksomhet: "nav" og tjenesteoppgave: "hjelpemidler"
- `7000000003` — Vergen til `7000000002`
- `7000000004` — Person med verge som ikke er i tjenestevirksomhet: "nav"
- `7000000005` — Person med verge som ikke har tjenesteoppgave hjelpemidler

## Bruk

Bruk FNR-ene ovenfor i testing for å få konsistente og forutsigbare testdata fremfor tilfeldige verdier.

Eksempel:
```
GET /api/person/70000000001
```

Dette returnerer en person med dødsdato 15. juni 2023, nyttig for å teste UI-oppførsel for avdøde personer.

---

## Kabal mock — simuler klage- og ankehendelser lokalt

Mock-serveren eksponerer endepunkter for å sende `BehandlingEvent`-meldinger fra Kabal på Kafka-topicen `klage.behandling-events.v1`. Slik kan du simulere at Kabal avslutter en klage- eller ankebehandling uten et ekte Kabal-miljø.

### Key og Value forklart

Fra kabal-api-kildekoden (`KafkaProducer.kt` + `BehandlingAvslutningService.kt`):

```kotlin
// Kabal sender slik:
aivenKafkaTemplate.send(topic, klagebehandlingId.toString(), json)
//                               ^^^ KEY = Kabals interne UUID (behandling.id / kabalReferanse)

// BehandlingEvent-felter:
kildeReferanse = behandling.kildeReferanse  // ← saksnummeret fra Superhelt, f.eks. "SH-000001"
kilde          = behandling.fagsystem.navn  // ← alltid "SUPERHELT"
kabalReferanse = behandling.id.toString()  // ← samme UUID som KEY
```

| Felt | Hva du setter inn | Eksempel |
|------|------------------|---------|
| **Key** | Kabals interne UUID for behandlingen (`kabalReferanse`) | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| **Value** | JSON-payload (se eksempel under) — plain JSON, ingen Spring-headere | se under |
| `kildeReferanse` i JSON | Saksnummeret fra Superhelt — **må finnes i databasen** | `SH-000001` |
| `kilde` i JSON | Må alltid være `"SUPERHELT"` — ellers ignoreres meldingen | `SUPERHELT` |
| `kabalReferanse` i JSON | Samme UUID som Key | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| `avsluttet` | ISO-8601 dato-tid | `2026-05-12T10:00:00` |

### Forutsetninger

Hele den lokale stacken må kjøre:
```shell
docker compose up          # starter Kafka, Postgres, Wonderwall og mock-oidc
# deretter: start DevApplication.kt i IDE-en
```

### Alle 8 event-typer — curl-kommandoer (44 kommandoer totalt)

Bytt `SH-000001` med et saksnummer som finnes i din lokale database.
Hver kommando genererer automatisk en ny unik `eventId` — trygt å kjøre flere ganger.

> ⚠️ **Merk for `BEHANDLING_FEILREGISTRERT`:** Denne markerer saken som feilregistrert i databasen. Kjør den på en testsak du ikke trenger videre.

#### 1. KLAGEBEHANDLING_AVSLUTTET — 9 utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=DELVIS_MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=STADFESTELSE"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=RETUR"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=OPPHEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=UGUNST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=TRUKKET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=AVVIST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/klage/SH-000001?utfall=HENLAGT"
```

#### 2. ANKEBEHANDLING_OPPRETTET — ingen utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke-opprettet/SH-000001"
```

#### 3. ANKEBEHANDLING_AVSLUTTET — 10 utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=DELVIS_MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=STADFESTELSE"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=OPPHEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=UGUNST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=HEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=HENVIST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=TRUKKET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=AVVIST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke/SH-000001?utfall=HENLAGT"
```

#### 4. ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET — ingen utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/anke-trygderetten/SH-000001"
```

#### 5. BEHANDLING_FEILREGISTRERT — 7 typer ⚠️
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=KLAGE"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=ANKE"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=ANKE_I_TRYGDERETTEN"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=OMGJOERINGSKRAV"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=BEGJAERING_OM_GJENOPPTAK"
curl -X POST "http://localhost:9080/kabal-mock/kafka/feilregistrert/SH-000001?type=BEGJAERING_OM_GJENOPPTAK_I_TRYGDERETTEN"
```
Valgfrie parametere: `navIdent` (default `Z999999`) og `reason` (default `Feilregistrert under testing`).

#### 6. BEHANDLING_ETTER_TRYGDERETTEN_OPPHEVET_AVSLUTTET — 9 utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=DELVIS_MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=STADFESTELSE"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=RETUR"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=OPPHEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=UGUNST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=TRUKKET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=AVVIST"
curl -X POST "http://localhost:9080/kabal-mock/kafka/etter-trygderetten/SH-000001?utfall=HENLAGT"
```

#### 7. OMGJOERINGSKRAVBEHANDLING_AVSLUTTET — 2 utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/omgjoeringskrav/SH-000001?utfall=MEDHOLD_ETTER_FVL_35"
curl -X POST "http://localhost:9080/kabal-mock/kafka/omgjoeringskrav/SH-000001?utfall=UGUNST"
```

#### 8. GJENOPPTAKSBEHANDLING_AVSLUTTET — 4 utfall
```shell
curl -X POST "http://localhost:9080/kabal-mock/kafka/gjenopptak/SH-000001?utfall=MEDHOLD_ETTER_FVL_35"
curl -X POST "http://localhost:9080/kabal-mock/kafka/gjenopptak/SH-000001?utfall=GJENOPPTATT_DELVIS_ELLER_FULLT_MEDHOLD"
curl -X POST "http://localhost:9080/kabal-mock/kafka/gjenopptak/SH-000001?utfall=GJENOPPTATT_OPPHEVET"
curl -X POST "http://localhost:9080/kabal-mock/kafka/gjenopptak/SH-000001?utfall=UGUNST"
```


### Alternativ: publiser via IntelliJ Kafka-plugin

Har du Kafka-plugin installert i IntelliJ (f.eks. **Kafkalytic** eller **Big Data Tools**), kan du produsere meldinger direkte fra IDE-en.

**Koble til lokal Kafka (én gang):**

1. Åpne **Kafka**-fanen i IntelliJ (vanligvis nederst eller i høyre sidepanel)
2. Klikk **+** eller **Add Kafka cluster**
3. Fyll inn:
   - **Name:** `my-local-kafka`
   - **Bootstrap servers:** `localhost:9092`
4. Klikk **Test connection** → skal vise grønt
5. Klikk **OK** / **Save**

**Send en melding til `klage.behandling-events.v1`:**

1. Utvid klyngen i Kafka-fanen → finn topicen **`klage.behandling-events.v1`**
2. Høyreklikk på topicen → velg **Produce message** (eller klikk ✉️-ikonet)
3. Fyll inn:
   - **Key:** Kabals interne UUID for behandlingen (= `kabalReferanse` i JSON), f.eks. `a1b2c3d4-e5f6-7890-abcd-ef1234567890`
   - **Value:** lim inn JSON-payload under
4. Klikk **Send** / **Produce**

> **Key = `kabalReferanse`** — dette er Kabals interne UUID for behandlingen, ikke saksnummeret. `kildeReferanse` _inne i_ JSON-payloaden er saksnummeret fra Superhelt.

**Eksempel – klagebehandling avsluttet med MEDHOLD:**
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "kildeReferanse": "SH-000001",
  "kilde": "SUPERHELT",
  "kabalReferanse": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "type": "KLAGEBEHANDLING_AVSLUTTET",
  "detaljer": {
    "klagebehandlingAvsluttet": {
      "avsluttet": "2026-05-12T10:00:00",
      "utfall": "MEDHOLD",
      "journalpostReferanser": ["JP-123456789", "JP-987654321"]
    }
  }
}
```

**Eksempel – ankebehandling avsluttet med STADFESTELSE:**
```json
{
  "eventId": "c9d1e2f3-4a5b-6c7d-8e9f-0a1b2c3d4e5f",
  "kildeReferanse": "SH-000001",
  "kilde": "SUPERHELT",
  "kabalReferanse": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "type": "ANKEBEHANDLING_AVSLUTTET",
  "detaljer": {
    "ankebehandlingAvsluttet": {
      "avsluttet": "2026-05-12T14:30:00",
      "utfall": "STADFESTELSE",
      "journalpostReferanser": ["JP-111222333"]
    }
  }
}
```

> **Viktig:**
> - `kildeReferanse` = saksnummer som **finnes i din lokale database**, format `SH-000001` (se URL i Superhelt: `/sak/SH-000042`)
> - `kilde` = alltid `"SUPERHELT"` — ellers ignoreres meldingen
> - `kabalReferanse` = samme UUID som **Key**-feltet — kan være en vilkårlig UUID

---

### Alternativ: publiser direkte via Kafka UI

Du kan også sende meldinger manuelt via Kafka UI på http://localhost:8080.

**Steg for steg:**

1. Åpne **http://localhost:8080** i nettleseren
2. Klikk på **`my-local-kafka`** i venstre meny
3. Klikk på **Topics** → velg topicen **`klage.behandling-events.v1`**
4. Klikk på **Produce Message** øverst til høyre
5. Fyll inn feltene:

   | Felt | Verdi |
   |------|-------|
   | **Key** | Kabals interne UUID = `kabalReferanse`, f.eks. `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
   | **Value** | JSON-payload (se eksempel under) |

6. Klikk **Produce Message** for å sende

**Eksempel – klagebehandling avsluttet med MEDHOLD:**
```json
{
  "eventId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "kildeReferanse": "SH-000001",
  "kilde": "SUPERHELT",
  "kabalReferanse": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "type": "KLAGEBEHANDLING_AVSLUTTET",
  "detaljer": {
    "klagebehandlingAvsluttet": {
      "avsluttet": "2026-05-12T10:00:00",
      "utfall": "MEDHOLD",
      "journalpostReferanser": ["JP-123456789", "JP-987654321"]
    }
  }
}
```

**Eksempel – ankebehandling avsluttet med STADFESTELSE:**
```json
{
  "eventId": "c9d1e2f3-4a5b-6c7d-8e9f-0a1b2c3d4e5f",
  "kildeReferanse": "SH-000001",
  "kilde": "SUPERHELT",
  "kabalReferanse": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "type": "ANKEBEHANDLING_AVSLUTTET",
  "detaljer": {
    "ankebehandlingAvsluttet": {
      "avsluttet": "2026-05-12T14:30:00",
      "utfall": "STADFESTELSE",
      "journalpostReferanser": ["JP-111222333"]
    }
  }
}
```

> **Merk:**
> - **Key** = `kabalReferanse` (Kabals interne UUID) — samme UUID som `kabalReferanse`-feltet i JSON
> - `kildeReferanse` = saksnummeret fra Superhelt — må finnes i databasen, format `SH-000001`
> - `kilde` = alltid `"SUPERHELT"`, ellers ignoreres meldingen

---

### Slik fungerer det

```
curl POST /kabal-mock/kafka/klage/{kildeReferanse}
        │
        ▼
MockKabalBehandlingEventProducer
        │   sender BehandlingEvent (KLAGEBEHANDLING_AVSLUTTET)
        ▼
Kafka-topic: klage.behandling-events.v1
        │
        ▼
KabalBehandlingEventConsumer (backend)
        │   behandler hendelsen, oppdaterer klagestatus
        ▼
Database (KabalEventEntity)
```

---

→ Tilbake til [hoved-README](../../README.md)
