# Mock Server - Test Persons

This mock server provides predefined test persons for testing various scenarios.

## Available Test Persons

### Regular Persons
Any 11-digit FNR not listed below will generate a random person.

### Persons Rejected by Tilgangsmaskin
- `40400000000` - Unknown person (UKJENT_PERSON)
- `40300000001` - Rejected due to habilitet (AVVIST_HABILITET)
- `40300000002` - Rejected due to death (AVVIST_AVDØD)
- `40300000006` - Rejected due to strongly confidential address (AVVIST_STRENGT_FORTROLIG_ADRESSE)
- `40300000007` - Rejected due to confidential address (AVVIST_FORTROLIG_ADRESSE)

### Persons with Address Protection (Approved by Tilgangsmaskin)
- `60000000001` - Strongly confidential address (STRENGT_FORTROLIG)
- `60000000002` - Confidential address (FORTROLIG)
- `60000000003` - Strongly confidential address abroad (STRENGT_FORTROLIG_UTLAND)

### Persons with Death Date
- `70000000001` - Person with death date: 2023-06-15

### Personer med verge eller fullmektig
- `7000000002` - Person med verge med tjenestevirksomhet: "nav" og tjenesteoppgave: "hjelpemidler"
- `7000000003` - Vergen til `7000000002`
- `7000000004` - Person med verge som ikke er i tjenestevirksomhet: "nav"
- `7000000005` - Person med verge som ikke har tjenesteoppgave hjelpemidler

## Usage

Use these FNRs when testing to get consistent, predictable test data instead of random values.

Example:
```
GET /api/person/70000000001
```

This will return a person with a death date of June 15, 2023, which you can use for testing UI behavior with deceased persons.

