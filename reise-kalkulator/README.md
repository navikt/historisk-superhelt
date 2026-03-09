# Reisekostnadskalkulator

Vite + React + TypeScript-applikasjon for å beregne reisekostnad mellom to helsestasjoner.

Applikasjonen er konfigurert for deploy til NAVIKT NAIS (namespace: `historisk`, team: `historisk`).

## Kom i gang

### Forutsetninger

- Node.js ≥ 20
- npm

### Installere avhengigheter

```bash
npm install
```

### Kjøre lokalt (dev)

```bash
npm run dev
```

Applikasjonen kjøres på [http://localhost:3001](http://localhost:3001).

For å bruke dev-miljø-variabler, kopier `.env.dev` til `.env.local`:

```bash
cp .env.dev .env.local
```

### Produksjonsbygg

```bash
npm run build
```

Bygde filer legges i `dist/`.

### Forhåndsvisning av prod-bygg

```bash
npm run preview
```

## Linting og formatering

Applikasjonen bruker **ESLint** og **Prettier**.

```bash
# Lint-sjekk
npm run lint

# Lint-sjekk og fiks automatisk
npm run lint:fix

# Formateringssjekk
npm run format:check

# Formater kode
npm run format
```

## Tester

```bash
npm run test
```

## Mappestruktur

```
reise-kalkulator/
├── src/
│   ├── components/          # Gjenbrukbare React-komponenter
│   │   └── TravelCostResult.tsx
│   ├── pages/               # Sidekomponenter
│   │   └── HomePage.tsx
│   ├── services/            # Tjenester og typer
│   │   └── travelCostService.ts
│   ├── utils/               # Hjelpefunksjoner
│   │   └── calculateCost.ts
│   ├── App.tsx
│   ├── main.tsx
│   └── index.css
├── .nais/
│   ├── app.yaml             # NAIS-applikasjonskonfigurasjon
│   ├── dev.json             # Dev-miljøvariabler
│   └── prod.json            # Prod-miljøvariabler
├── .env.dev                 # Dev-miljøvariabler (plassholdere)
├── .env.prod                # Prod-miljøvariabler (plassholdere)
├── Dockerfile               # Docker-image for NAIS-deploy
├── nginx.conf               # Nginx-konfigurasjon for prod
├── vite.config.ts
├── tsconfig.json
├── eslint.config.js
├── .prettierrc
└── package.json
```

## Deploy til NAVIKT NAIS

### Manuell deploy

1. Bygg Docker-image:

```bash
docker build -t ghcr.io/navikt/reise-kalkulator:latest .
docker push ghcr.io/navikt/reise-kalkulator:latest
```

2. Deploy til dev:

```bash
nais deploy \
  --cluster=dev-gcp \
  --resource=.nais/app.yaml \
  --vars=.nais/dev.json \
  --image=ghcr.io/navikt/reise-kalkulator:latest
```

3. Deploy til prod:

```bash
nais deploy \
  --cluster=prod-gcp \
  --resource=.nais/app.yaml \
  --vars=.nais/prod.json \
  --image=ghcr.io/navikt/reise-kalkulator:latest
```

### Automatisk deploy (CI/CD)

GitHub Actions-workflowen `.github/workflows/reise-kalkulator.yml` bygger og deployer automatisk ved push til `main`.

- Push til `main` → deploy til **dev-gcp**
- Etter vellykket dev-deploy → deploy til **prod-gcp**

## Miljøvariabler

| Variabel | Beskrivelse | Eksempel |
|---|---|---|
| `VITE_APP_ENV` | Miljønavn | `dev` eller `prod` |
| `VITE_API_URL` | Backend API-URL | `https://reise-kalkulator.intern.nav.no` |
| `VITE_APP_TITLE` | Applikasjonstittel | `Reisekostnadskalkulator` |
