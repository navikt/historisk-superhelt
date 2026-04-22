---
applyTo: "frontend/src/**/*.{tsx,ts}"
---

# React med Aksel Design System

Standarder for React-apper (TanStack Router/Query) med Aksel: spacing-tokens, desktop-først layout og komponentmønstre.

Dette repoet er et **saksbehandlingsverktøy** som primært brukes på desktop. Design desktop-først og tilpass ned til `md` ved behov — ikke mobil-først.

## Spacing-regler

**VIKTIG**: Bruk alltid Nav DS spacing-tokens, aldri Tailwind padding/margin.

### ✅ Riktig — desktop-først

```tsx
import { Box, VStack, HGrid } from "@navikt/ds-react";

// Side-container — desktop er basen
<Box
  paddingBlock="space-24"
  paddingInline="space-40"
>
  {children}
</Box>

// Responsivt ved behov — desktop-først, shrink ned
<Box
  paddingBlock={{ lg: "space-32", md: "space-24" }}
  paddingInline={{ lg: "space-40", md: "space-24" }}
>
  {children}
</Box>

// Retningsbestemt padding
<Box
  paddingBlock="space-16"    // Topp og bunn
  paddingInline="space-24"   // Venstre og høyre
>
```

### ❌ Feil

```tsx
// Aldri bruk Tailwind padding/margin
<div className="p-4 md:p-6">   // ❌ Feil
<div className="mx-4 my-2">    // ❌ Feil
<Box padding="4">              // ❌ Feil — Box krever space-prefiks: padding="space-4"
<VStack gap="space-4">         // ❌ Feil — gap bruker numerisk shorthand: gap="4"

// Aldri mobil-først breakpoints
<Box padding={{ xs: "space-16", md: "space-24" }}>  // ❌ Mobil-først
```

## Spacing-tokens

Aksel DS bruker **to ulike token-systemer** avhengig av prop:

| Prop | Komponent | Format | Eksempel |
|------|-----------|--------|---------|
| `padding`, `paddingBlock`, `paddingInline` | `Box` | `space-*` streng | `"space-16"` |
| `gap` | `VStack`, `HStack`, `HGrid` | Numerisk shorthand | `"4"`, `"8"` |

`Box` padding-tokens (alltid med `space-`-prefiks):

- `space-4` (4px)
- `space-8` (8px)
- `space-12` (12px)
- `space-16` (16px)
- `space-20` (20px)
- `space-24` (24px)
- `space-32` (32px)
- `space-40` (40px)

`gap`-verdier for layout-komponenter (numerisk, uten `space-`-prefiks):
`"0"` `"1"` `"2"` `"3"` `"4"` `"5"` `"6"` `"7"` `"8"` `"10"` `"12"` `"16"` `"20"` `"24"` `"32"`

## Responsivt design — desktop-først

Desktop er standardtilstand. Bruk breakpoints kun når layouten skal tilpasses ned:

- `lg`: 1024px (desktop — primær)
- `md`: 768px (nettbrett — shrink ned)
- `sm`: 480px (sjelden aktuelt)
- `xs`: 0px (ikke i bruk som base)

```tsx
// ✅ Desktop-først — tilpass ned
<HGrid columns={{ lg: 3, md: 2 }} gap="4">
  {items.map(item => <Card key={item.id} {...item} />)}
</HGrid>

<Box padding={{ lg: "space-32", md: "space-24" }}>
```

## Komponentmønstre

### Layout-komponenter

```tsx
import { Box, VStack, HStack, HGrid } from "@navikt/ds-react";

// Vertikal stack med mellomrom
<VStack gap="4">
  <Komponent1 />
  <Komponent2 />
</VStack>

// Horisontal stack
<HStack gap="4" align="center">
  <Icon />
  <Text />
</HStack>

// Desktop-grid med fallback
<HGrid columns={{ lg: 3, md: 2 }} gap="4">
  {/* Grid-elementer */}
</HGrid>
```

### Typografi

```tsx
import { Heading, BodyShort, Label } from "@navikt/ds-react";

<Heading size="large|medium|small" level="1-6">
  Tittel
</Heading>

<BodyShort size="large|medium|small">
  Vanlig tekstinnhold
</BodyShort>

<BodyShort weight="semibold">
  Halvfet tekst
</BodyShort>

<Label size="large|medium|small">
  Skjemaetikett
</Label>
```

### Bakgrunnsfarger

```tsx
<Box background="surface-default">     {/* Hvit */}
<Box background="surface-subtle">      {/* Lys grå */}
<Box background="surface-action-subtle">  {/* Lys blå */}
<Box background="surface-success-subtle"> {/* Lys grønn */}
<Box background="surface-warning-subtle"> {/* Lys oransje */}
<Box background="surface-danger-subtle">  {/* Lys rød */}
```

## TanStack Router

```tsx
// src/routes/saker/$sakId.tsx
import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/saker/$sakId")({
  component: SakDetaljer,
});

function SakDetaljer() {
  const { sakId } = Route.useParams();
  return <div>Sak {sakId}</div>;
}
```

## TanStack Query

```tsx
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

// Datahenting
const { data, isLoading, error } = useQuery({
  queryKey: ["saker", sakId],
  queryFn: () => fetchSak(sakId),
});

if (isLoading) return <Loader title="Laster..." />;
if (error) return <Alert variant="error">Kunne ikke laste sak</Alert>;

// Mutasjon med cache-invalidering
const queryClient = useQueryClient();
const mutation = useMutation({
  mutationFn: (data: OppdaterSakRequest) => oppdaterSak(sakId, data),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ["saker", sakId] }),
});
```

## Genererte typer fra backend

```tsx
// Typer genereres med: pnpm run openapi-ts
import type { Sak } from "@generated";

// Lokale utility-typer utledes fra genererte typer
type SakStatusType = Sak["status"];
```

## Pakkebehandler

Bruk `pnpm`:

```bash
pnpm install
pnpm run test
pnpm run biome:write
pnpm run openapi-ts
```

## Boundaries

### ✅ Always

- Bruk Aksel Design System-komponenter
- Bruk spacing-tokens med `space-`-prefiks
- Desktop-først — `lg`/`md` som primær breakpoint
- Importer genererte typer via `@generated`-alias
- Eksplisitt feilhåndtering i datahenting

### ⚠️ Ask First

- Egendefinerte CSS-klasser utenfor Aksel
- Avvik fra Aksel-mønstre for tilgjengelighet

### 🚫 Never

- Tailwind padding/margin (`p-*`, `m-*`)
- Numerisk verdi uten `space-`-prefiks på `Box` padding (`padding="4"` → `padding="space-4"`)
- `space-*`-prefiks på layout-komponent `gap` (`gap="space-4"` → `gap="4"`)
- Mobil-først breakpoints (`xs` som base)
- Ignorere tilgjengelighetskrav

## Relatert

| Resource | Bruk til |
|----------|----------|
| `@aksel-agent` | Aksel DS komponentmønstre og spacing-tokens |
| `@accessibility-agent` | WCAG 2.1/2.2 og tilgjengelighetstesting |
| `aksel-spacing` skill | Responsiv spacing-token referanse |
| `playwright-testing` skill | E2E-testing med Playwright og axe-core |
