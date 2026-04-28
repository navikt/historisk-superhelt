# Frontend – Superhelt

React 19-frontend for Superhelt, bygget med TanStack Router/Query og Nav Aksel Design System.

## Kom i gang

```bash
pnpm install
pnpm start   # dev-server på :3000 med hot reload
```

Innlogging skjer via http://localhost:4000 (Wonderwall). Backend og `docker compose up` må kjøre først – se [rotmappens README](../README.md).

## Produksjonsbygg

```bash
pnpm run build
```

## Tester

```bash
pnpm run test
```

## Generere API-typer

Typer genereres automatisk fra backend sin OpenAPI-spec. Backend må kjøre lokalt på `:8080`.

```bash
pnpm run openapi-ts
```

Genererte typer havner i `generated/` og importeres via `@generated`-aliaset:
```ts
import type { Sak } from "@generated"
```

## Formatering og linting

```bash
pnpm run biome        # sjekk
pnpm run biome:write  # sjekk og skriv endringer
```