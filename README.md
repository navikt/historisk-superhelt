# Superhelt

Saksbehanding og Utbetaling På Enkle Regler innen Helsetjenster

>Alle trenger hjelp av en superhelt en gang i blant

App som skal tilby saksbehandling og engangsutbetaling på en rekke enkle stønader. 
Målet med appen er å erstatte infotrygd rutiner  HT-MV, SB-SA og GE-PP

### Stønader som skal støttes
- Ortoser
- Parykk
- Fottøy
- Proteser
- Reiseutgifter

## Lokal utvikling

### TestContainers
For å kjøre tester lokalt må du ha Docker installert og kjørende på maskinen din. TestContainers vil automatisk laste ned og starte nødvendige containere for testene.

For Colima på Mac, sjekk https://golang.testcontainers.org/system_requirements/using_colima/

### Test personer
Mock-serveren har forhåndsdefinerte testpersoner for ulike scenarier. Se [mocks/mock-server/README.md](./mocks/mock-server/README.md) for fullstendig oversikt.

Eksempler:
- `70000000001` - Person med dødsdato 2023-06-15
- `70000000002` - Person med verge
- `60000000001` - Person med strengt fortrolig adresse
- `40300000001` - Person som avvises av tilgangsmaskin (habilitet)

### Tjenester som appen trenger
```shell
docker-compose up
```
### Backend
Start backend i ved å kjøre [DevApplication.kt](./backend/src/test/kotlin/no/nav/historisk/superhelt/DevApplication.kt) i IDE

Appen er da tilgjengelig på http://localhost:4000 med pålogging via wonderwall. Frontend blir oppdatert ved bygg av frontend

### Frontend
```shell
# i /frontend
npm start 
```

Appen er da tilgjengelig på http://localhost:3000 med hot reload, devtools mm ++
Pålogging må skje via http://localhost:4000

#### Formatering og linting
Prosjektet bruker Biome for formatering og linting av frontend koden

```shell
npm run biome # kjører formatter og linter
npm run biome:write # kjører formatter og linter, og skriver endringer til filene
```

![nettverksopplegg](./docs/super-docker-compose.png)
