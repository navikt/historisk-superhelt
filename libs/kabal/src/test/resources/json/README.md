# Kabal API Test Data - JSON Resources

This directory contains example JSON files for testing the Kabal (Klage og Anke) API integration.

## Files Overview

### Request Examples

#### 1. `minimal-klage-request.json`
**Purpose:** Minimal valid KLAGE (complaint) request with only required fields

**Use Cases:**
- Testing basic client functionality
- Validating required field handling
- Mock server testing with minimal data

**Key Fields:**
- `type`: KLAGE
- `sakenGjelder`: Single person identifier
- `klager`: Same as sakenGjelder (person filed own complaint)
- `fagsak`: K9 system with basic ID

**Example:**
```json
{
  "type": "KLAGE",
  "sakenGjelder": { "id": { "type": "PERSON", "verdi": "12345678901" } },
  "klager": { "id": { "type": "PERSON", "verdi": "12345678901" } },
  "fagsak": { "fagsakId": "123456", "fagsystem": "K9" }
}
```

---

#### 2. `full-klage-request.json`
**Purpose:** Complete KLAGE request with all possible fields populated

**Use Cases:**
- Testing full request serialization
- Validating all optional fields
- Integration testing with complete data
- Documentation example

**Key Features:**
- Legal representative (prosessfullmektig) with full address
- Multiple legal statutes (hjemlers)
- Multiple attached documents (journalposter)
- All optional fields populated
- Dates and datetimes included

**Example Structure:**
```json
{
  "type": "KLAGE",
  "sakenGjelder": { ... },
  "klager": { ... },
  "prosessfullmektig": {
    "id": { "type": "PERSON", "verdi": "98765432101" },
    "navn": "Advokat Hansen",
    "adresse": { ... }
  },
  "fagsak": { ... },
  "tilknyttedeJournalposter": [
    { "type": "BRUKERS_KLAGE", "journalpostId": "jp-001" },
    { "type": "OPPRINNELIG_VEDTAK", "journalpostId": "jp-002" }
  ],
  ...
}
```

---

#### 3. `full-anke-request.json`
**Purpose:** Complete ANKE (appeal) request demonstrating appeal-specific fields

**Use Cases:**
- Testing ANKE case type
- Validating appeal workflow
- Testing appeal-specific journals and statutes
- Integration testing for higher-level appeals

**Key Differences from KLAGE:**
- `type`: ANKE (not KLAGE)
- Journalposter include: `BRUKERS_ANKE`, `KLAGE_VEDTAK`
- Longer frist (typically 3 months)
- Often references previous klage vedtak
- `forrigeBehandlendeEnhet`: References klage committee

**Example:**
```json
{
  "type": "ANKE",
  "tilknyttedeJournalposter": [
    { "type": "BRUKERS_ANKE", "journalpostId": "jp-003" },
    { "type": "KLAGE_VEDTAK", "journalpostId": "jp-004" }
  ],
  ...
}
```

---

#### 4. `virksomhet-klage-request.json`
**Purpose:** KLAGE request from/about a VIRKSOMHET (organization/business)

**Use Cases:**
- Testing organization identifier handling
- Business case testing
- Validating different ident types
- Testing non-person complainants

**Key Differences:**
- `type`: VIRKSOMHET in both `sagenGjelder` and `klager`
- `verdi`: Organization number (9 digits) instead of person ID
- Often relates to different ytelses like BAR_BAR (barnetrygd)
- May have multiple related documents

**Example:**
```json
{
  "sakenGjelder": {
    "id": {
      "type": "VIRKSOMHET",
      "verdi": "987654321"
    }
  },
  "klager": {
    "id": {
      "type": "VIRKSOMHET",
      "verdi": "987654321"
    }
  },
  ...
}
```

---

### Response Examples

#### 5. `successful-response.json`
**Purpose:** Example of successful response from Kabal API

**Use Cases:**
- Testing response parsing
- Mock server configuration
- Integration test setup
- Documentation example

**Fields:**
- `behandlingId`: Unique case ID in Kabal system
- `mottattDato`: ISO timestamp of receipt
- `journalpostId`: Optional journal post ID if created
- `feilmeldinger`: Empty list for success

**Example:**
```json
{
  "behandlingId": "behandling-2026-001",
  "mottattDato": "2026-03-06T10:00:00",
  "journalpostId": "jp-kabal-001",
  "feilmeldinger": []
}
```

---

#### 6. `error-response.json`
**Purpose:** Response with validation errors from Kabal API

**Use Cases:**
- Testing error handling
- Validation error testing
- Error message parsing
- Exception scenario testing

**Common Error Scenarios:**
- Invalid hjemler (legal statutes)
- Missing required journalposter
- Invalid klager relationship
- Business rule violations

**Example:**
```json
{
  "behandlingId": "behandling-2026-002",
  "mottattDato": "2026-03-06T11:00:00",
  "journalpostId": null,
  "feilmeldinger": [
    "Ugyldig hjemmel FVL_99",
    "Manglende journalpost av type OPPRINNELIG_VEDTAK",
    "Klager må være samme som saksbehandler eller ha fullmakt"
  ]
}
```

---

## Enum Reference

### SakType
- **KLAGE** - Complaint/appeal to first instance
- **ANKE** - Higher-level appeal

### IdentType
- **PERSON** - Person identifier (fnr/dnr)
- **VIRKSOMHET** - Organization/business number

### JournalpostType
- **BRUKERS_SOEKNAD** - User application
- **OPPRINNELIG_VEDTAK** - Original decision
- **BRUKERS_KLAGE** - User complaint
- **BRUKERS_ANKE** - User appeal
- **BRUKERS_OMGJOERINGSKRAV** - User request for revision
- **BRUKERS_BEGJAERING_OM_GJENOPPTAK** - User request for reopening
- **OVERSENDELSESBREV** - Transmittal letter
- **KLAGE_VEDTAK** - Complaint decision
- **ANNET** - Other

### Ytelse (Benefits)
- **OMS_OMP** - Omsorgspengar (care benefit)
- **BAR_BAR** - Barnetrygd (child benefit)
- **PEN_ALD** - Alderspensjon (old age pension)
- **UFO_UFO** - Uføretrygd (disability benefit)
- And many more...

### Hjemler (Legal Statutes)
Common examples:
- **FVL_11** - General administrative procedure law
- **FVL_12** - Administrative procedure law
- **FVL_35** - Appeal deadline law
- **FTRL_2_1** - National Insurance Act 2-1
- **FTRL_3_2** - National Insurance Act 3-2
- And many others...

---

## Using These Files in Tests

### Example: Loading Test Data
```kotlin
import java.nio.file.Files
import java.nio.file.Paths

@Test
fun `test with json file`() {
    val json = Files.readString(
        Paths.get("src/test/resources/json/full-klage-request.json")
    )
    val request = objectMapper.readValue(json, SendSakV4Request::class.java)
    
    // Use request in test
    val response = kabalClient.sendSakV4(request)
    
    // Verify response
    assertThat(response.behandlingId).isNotNull()
}
```

### Example: Mock Server with JSON
```kotlin
@Test
fun `mock server returns error response`() {
    val errorJson = Files.readString(
        Paths.get("src/test/resources/json/error-response.json")
    )
    
    mockServer.expect(requestTo("/api/oversendelse/v4/sak"))
        .andRespond(
            withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorJson)
        )
    
    // Test error handling
}
```

---

## Field Descriptions

### SendSakV4Request Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| type | SakType | ✅ | KLAGE or ANKE |
| sakenGjelder | SakenGjelder | ✅ | Person/org the case concerns |
| klager | Klager | ✅ | Person/org filing complaint |
| prosessfullmektig | Prosessfullmektig | ❌ | Legal representative |
| fagsak | Fagsak | ✅ | Source system info |
| kildeReferanse | String | ❌ | Source system reference |
| dvhReferanse | String | ❌ | DVH reporting reference |
| hjemler | List<String> | ❌ | Legal statutes involved |
| forrigeBehandlendeEnhet | String | ❌ | Previous handling unit |
| tilknyttedeJournalposter | List | ❌ | Related documents |
| brukersKlageMottattVedtaksinstans | LocalDate | ❌ | When complaint received |
| frist | LocalDate | ❌ | Deadline for resolution |
| sakMottattKaTidspunkt | LocalDateTime | ❌ | When case received |
| ytelse | String | ❌ | Benefit type |
| kommentar | String | ❌ | Handler comments |
| hindreAutomatiskSvarbrev | Boolean | ❌ | Suppress auto-response |
| saksbehandlerIdentForTildeling | String | ❌ | Handler for assignment |

---

## Date Format

- **Date Fields**: `yyyy-MM-dd` (e.g., `2026-03-06`)
- **DateTime Fields**: `yyyy-MM-dd'T'HH:mm` (e.g., `2026-03-05T10:00`)

---

## Testing Patterns

### Pattern 1: Minimal Valid Request
Use `minimal-klage-request.json` for:
- Testing basic happy path
- Validating required field handling
- Simple mock scenarios

### Pattern 2: Full Request
Use `full-klage-request.json` for:
- Testing all field handling
- Integration scenarios
- Documentation examples

### Pattern 3: Edge Cases
Use `virksomhet-klage-request.json` for:
- Testing different ident types
- Organization-related cases
- Non-standard scenarios

### Pattern 4: Error Handling
Use `error-response.json` for:
- Testing error parsing
- Validation error handling
- Exception scenarios

---

## Notes

- All identifiers are examples and do not represent real data
- Dates are all in 2026 for consistency in examples
- Fields marked as optional (❌ in required column) can be omitted
- Mock server tests can use these files directly
- JSON is valid and can be parsed by Jackson ObjectMapper
- Files follow Kabal API v4 specification

---

**Created:** March 6, 2026  
**Format:** JSON  
**API Version:** Kabal v4  
**Last Updated:** March 6, 2026

