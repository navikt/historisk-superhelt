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

![nettverksopplegg](./docs/super-docker-compose.png)