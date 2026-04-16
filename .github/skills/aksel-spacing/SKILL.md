---
name: aksel-spacing
description: Responsiv layout med Aksel spacing-tokens og Box, VStack, HStack og HGrid
---

# Aksel Spacing Skill

Responsive layout-mønstre med Navs Aksel Design System spacing-tokens.

## Viktig regel

**Aldri bruk Tailwind padding/margin (`p-`, `m-`, `px-`, `py-`) med Aksel-komponenter.**

Bruk alltid Aksel spacing-tokens for Box padding: `space-16`, `space-24`, `space-32`, osv.
For VStack/HStack/HGrid `gap` brukes numerisk shorthand: `"4"`, `"6"`, `"8"`, osv.

## Page container-mønster

```typescript
import { Box, VStack } from '@navikt/ds-react';

export default function Page() {
  return (
    <main className="max-w-7xl mx-auto">
      <Box
        paddingBlock={{ xs: 'space-32', md: 'space-48' }}
        paddingInline={{ xs: 'space-16', md: 'space-40' }}
      >
        <VStack gap={{ xs: "6", md: "8" }}>
          {/* Sideinnhold */}
        </VStack>
      </Box>
    </main>
  );
}
```

## Card-mønster

```typescript
import { Box, VStack, Heading, BodyShort } from '@navikt/ds-react';

export function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Box
      background="surface-default"
      padding={{ xs: 'space-24', md: 'space-32' }}
      borderRadius="large"
      borderWidth="1"
      borderColor="border-subtle"
    >
      <VStack gap="4">
        <Heading size="medium">{title}</Heading>
        <BodyShort>{children}</BodyShort>
      </VStack>
    </Box>
  );
}
```

## Form-layout

```typescript
import { VStack, HStack, TextField, Button } from '@navikt/ds-react';

export function UserForm() {
  return (
    <VStack gap="6">
      {/* Input-felt med konsistent vertikal spacing */}
      <VStack gap="4">
        <TextField label="Fornavn" />
        <TextField label="Etternavn" />
        <TextField label="E-post" type="email" />
      </VStack>

      {/* Button row med horisontal spacing */}}
      <HStack gap="4" justify="end">
        <Button variant="secondary">Avbryt</Button>
        <Button variant="primary">Send inn</Button>
      </HStack>
    </VStack>
  );
}
```

## Dashboard-grid

```typescript
import { HGrid, Box, VStack, Heading } from '@navikt/ds-react';

export function Dashboard() {
  return (
    <VStack gap={{ xs: "6", md: "8" }}>
      <Heading size="xlarge">Dashboard</Heading>

      {/* Responsiv grid: 1 kolonne mobil, 2 nettbrett, 4 desktop */}
      <HGrid gap="4" columns={{ xs: 1, sm: 2, lg: 4 }}>
        <MetricCard title="Brukere" value="1 234" />
        <MetricCard title="Inntekt" value="5 678" />
        <MetricCard title="Bestillinger" value="910" />
        <MetricCard title="Vekst" value="+12 %" />
      </HGrid>

      {/* Innholdsområde */}
      <Box
        background="surface-subtle"
        padding={{ xs: 'space-24', md: 'space-32' }}
        borderRadius="large"
      >
        {/* Innhold */}
      </Box>
    </VStack>
  );
}
```

## Two-column layout

```typescript
import { HGrid, Box, VStack } from '@navikt/ds-react';

export function TwoColumnLayout() {
  return (
    <HGrid gap="6" columns={{ xs: 1, md: 2 }}>
      {/* Venstre kolonne */}
      <Box
        background="surface-default"
        padding={{ xs: 'space-24', md: 'space-32' }}
        borderRadius="large"
      >
        <VStack gap="4">
          {/* Venstre innhold */}
        </VStack>
      </Box>

      {/* Høyre kolonne */}
      <Box
        background="surface-subtle"
        padding={{ xs: 'space-24', md: 'space-32' }}
        borderRadius="large"
      >
        <VStack gap="4">
          {/* Høyre innhold */}
        </VStack>
      </Box>
    </HGrid>
  );
}
```

## Filter Section Pattern

```typescript
import { Box, VStack, HGrid, Select, TextField, Heading } from '@navikt/ds-react';

export function FilterSection() {
  return (
    <Box
      background="surface-subtle"
      padding={{ xs: 'space-16', md: 'space-24' }}
      borderRadius="large"
    >
      <VStack gap="4">
        <Heading size="small">Filters</Heading>

        {/* Responsive filter inputs */}
        <HGrid gap="4" columns={{ xs: 1, md: 3 }}>
          <Select label="Department">
            <option>All</option>
          </Select>

          <Select label="Status">
            <option>All</option>
          </Select>

          <TextField label="Search" />
        </HGrid>
      </VStack>
    </Box>
  );
}
```

## Spacing Tokens Reference

```typescript
"space-4";  // 4px
"space-8";  // 8px
"space-12"; // 12px
"space-16"; // 16px  ← Form field gaps
"space-20"; // 20px
"space-24"; // 24px  ← Card padding (mobile)
"space-32"; // 32px  ← Card padding (desktop), section gaps
"space-40"; // 40px  ← Page padding (desktop)
"space-48"; // 48px  ← Page padding block (desktop)
```

## Responsive Breakpoints

```typescript
xs: "0px"; // Mobile (default)
sm: "480px"; // Large mobile
md: "768px"; // Tablet
lg: "1024px"; // Desktop
xl: "1280px"; // Large desktop
```

## Common Patterns

```typescript
// ✅ Page padding
paddingBlock={{ xs: 'space-32', md: 'space-48' }}
paddingInline={{ xs: 'space-16', md: 'space-40' }}

// ✅ Card padding
padding={{ xs: 'space-24', md: 'space-32' }}

// ✅ Section gaps (VStack/HStack/HGrid — numerisk shorthand)
gap={{ xs: "6", md: "8" }}

// ✅ Form field gaps
gap="4"

// ✅ Button group gaps
gap="4"

// ❌ NEVER use Tailwind
className="p-4 m-2"  // WRONG!
className="px-6 py-4"  // WRONG!
```
