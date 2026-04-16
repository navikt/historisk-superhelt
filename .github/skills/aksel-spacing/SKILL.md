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
    <main>
      <Box
        paddingBlock="space-32"
        paddingInline="space-40"
      >
        <VStack gap="8">
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
      padding="space-24"
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
    <VStack gap="8">
      <Heading size="xlarge">Dashboard</Heading>

      {/* Desktop-grid: 4 kolonner, 2 på tablet */}
      <HGrid gap="4" columns={{ lg: 4, md: 2 }}>
        <MetricCard title="Brukere" value="1 234" />
        <MetricCard title="Inntekt" value="5 678" />
        <MetricCard title="Bestillinger" value="910" />
        <MetricCard title="Vekst" value="+12 %" />
      </HGrid>

      {/* Innholdsområde */}
      <Box
        background="surface-subtle"
        padding="space-24"
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
    <HGrid gap="6" columns={{ lg: 2, md: 1 }}>
      {/* Venstre kolonne */}
      <Box
        background="surface-default"
        padding="space-24"
        borderRadius="large"
      >
        <VStack gap="4">
          {/* Venstre innhold */}
        </VStack>
      </Box>

      {/* Høyre kolonne */}
      <Box
        background="surface-subtle"
        padding="space-24"
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
      padding="space-24"
      borderRadius="large"
    >
      <VStack gap="4">
        <Heading size="small">Filters</Heading>

        <HGrid gap="4" columns={{ lg: 3, md: 2 }}>
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
"space-24"; // 24px  ← Card padding
"space-32"; // 32px  ← Section gaps
"space-40"; // 40px  ← Page padding inline
"space-48"; // 48px  ← Page padding block
```

## Responsive Breakpoints — desktop-først

```typescript
lg: "1024px"; // Desktop (primær)
md: "768px";  // Tablet (tilpass ned)
sm: "480px";  // Sjelden aktuelt
xs: "0px";    // Ikke i bruk som base
```

## Common Patterns

```typescript
// ✅ Page padding — fast desktop-verdi
paddingBlock="space-32"
paddingInline="space-40"

// ✅ Page padding — responsivt (tilpass ned)
paddingBlock={{ lg: "space-48", md: "space-32" }}
paddingInline={{ lg: "space-40", md: "space-24" }}

// ✅ Card padding
padding="space-24"

// ✅ Grid — desktop-først
columns={{ lg: 4, md: 2 }}

// ✅ Section gaps (VStack/HStack/HGrid — numerisk shorthand)
gap="8"

// ✅ Form field gaps
gap="4"

// ✅ Button group gaps
gap="4"

// ❌ NEVER use Tailwind
className="p-4 m-2"  // WRONG!
className="px-6 py-4"  // WRONG!
```
