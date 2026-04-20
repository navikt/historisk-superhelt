---
name: aksel-component
description: Scaffold en responsiv React-komponent med Aksel Design System og riktige spacing-tokens
---

Du lager en ny React-komponent med Navs Aksel Design System.

## Viktige regler

1. **Aldri bruk Tailwind padding/margin** (`p-*`, `m-*`, `px-*`, `py-*`)
2. **Alltid bruk Aksel spacing-tokens** med `space-`-prefiks
3. **Desktop-først**, responsivt design med breakpoints: `lg`, `md` som primær — ikke `xs`-basert
4. **Bruk Aksel-komponenter**: Box, VStack, HGrid, Heading, BodyShort, Button, etc.

## Spør brukeren

1. **Komponentnavn**: Hva heter komponenten? (PascalCase)
2. **Formål**: Hva gjør komponenten?
3. **Layout**: Card, listeelement, form, dashboard-seksjon, etc.?
4. **Responsiv**: Skal layouten endre seg på ulike skjermstørrelser?

## Komponentmal

```tsx
import { Box, VStack, Heading, BodyShort } from "@navikt/ds-react";

interface {ComponentName}Props {
  title: string;
  description?: string;
  // Legg til flere props etter behov
}

export function {ComponentName}({
  title,
  description
}: {ComponentName}Props) {
  return (
    <Box
      background="surface-subtle"
      padding="space-24"
      borderRadius="large"
    >
      <VStack gap="4">
        <Heading size="medium" level="2">
          {title}
        </Heading>
        {description && (
          <BodyShort>
            {description}
          </BodyShort>
        )}
      </VStack>
    </Box>
  );
}
```

## Vanlige mønstre

### Card-komponent

```tsx
<Box
  background="surface-subtle"
  padding="space-24"
  borderRadius="large"
>
  <VStack gap="4">
    <Heading size="medium" level="3">
      {title}
    </Heading>
    <BodyShort>{description}</BodyShort>
  </VStack>
</Box>
```

### Responsiv grid-layout

```tsx
<HGrid columns={{ lg: 3, md: 2 }} gap="4">
  {items.map((item) => (
    <Card key={item.id} {...item} />
  ))}
</HGrid>
```

### Form-seksjon

```tsx
<Box paddingBlock="space-24">
  <VStack gap="8">
    <Heading size="large" level="2">
      Form Title
    </Heading>
    <VStack gap="4">
      <TextField label="Felt 1" />
      <TextField label="Felt 2" />
      <Button>Send inn</Button>
    </VStack>
  </VStack>
</Box>
```

### Dashboard Section

```tsx
<Box background="surface-default" padding="space-24" borderRadius="medium">
  <VStack gap="6">
    <HStack justify="space-between" align="center">
      <Heading size="large" level="2">
        Section Title
      </Heading>
      <Button variant="secondary" size="small">
        Action
      </Button>
    </HStack>
    <HGrid columns={{ lg: 4, md: 2 }} gap="4">
      {metrics.map((metric) => (
        <MetricCard key={metric.id} {...metric} />
      ))}
    </HGrid>
  </VStack>
</Box>
```

### Page Container

```tsx
<main>
  <Box paddingBlock="space-24" paddingInline="space-40">
    <VStack gap="8">{/* Page content */}</VStack>
  </Box>
</main>
```

## Available Aksel Components

### Layout

- `Box` - Container with spacing, background, radius
- `VStack` - Vertical stack with gap
- `HStack` - Horizontal stack with gap
- `HGrid` - Responsive grid

### Typography

- `Heading` - size: "large" | "medium" | "small", level: 1-6
- `BodyShort` - size: "large" | "medium" | "small"
- `BodyLong` - For longer text blocks
- `Label` - For form labels
- `Detail` - For supplementary info

### Interactive

- `Button` - variant: "primary" | "secondary" | "tertiary"
- `TextField` - Text input
- `Select` - Dropdown
- `Checkbox`, `Radio`, `Switch`

### Feedback

- `Alert` - variant: "info" | "success" | "warning" | "error"
- `Loader` - Loading spinner
- `HelpText` - Contextual help

## Spacing Tokens

Always use these tokens:

- `space-4` (4px)
- `space-8` (8px)
- `space-12` (12px)
- `space-16` (16px) - Common default
- `space-20` (20px)
- `space-24` (24px) - Common for cards
- `space-32` (32px)
- `space-40` (40px) - Common for page padding

## Background Colors

```tsx
background = "surface-default"; // White
background = "surface-subtle"; // Light gray
background = "surface-action-subtle"; // Light blue
background = "surface-success-subtle"; // Light green
background = "surface-warning-subtle"; // Light orange
background = "surface-danger-subtle"; // Light red
```

## Responsive Breakpoints

Desktop-først — bruk `lg`/`md` som primær, tilpass ned ved behov:

```tsx
// ✅ Desktop-først
padding="space-24"                                   // Fast desktop-verdi
padding={{ lg: "space-32", md: "space-24" }}        // Litt komprimert på tablet

columns={{ lg: 3, md: 2 }}  // Desktop 3-kolonne, tablet 2-kolonne
gap="4"                      // Fast gap (sjelden behov for responsiv gap)
```

Breakpoints:

- `lg`: 1024px (desktop — primær)
- `md`: 768px (tablet — tilpass ned)
- `sm`: 480px (sjelden aktuelt)
- `xs`: 0px (ikke i bruk som base)

## Testing

Create a test file `{component-name}.test.tsx`:

```tsx
import { render, screen } from "@testing-library/react";
import { ComponentName } from "./component-name";

describe("ComponentName", () => {
  it("should render title", () => {
    render(<ComponentName title="Test Title" />);
    expect(screen.getByText("Test Title")).toBeInTheDocument();
  });
});
```

## Checklist

After generating the component, verify:

- ✅ No Tailwind padding/margin utilities
- ✅ All spacing uses `space-` prefix tokens
- ✅ Responsive design with breakpoints
- ✅ TypeScript props interface
- ✅ Accessible markup (proper heading levels, labels)
- ✅ Component exported from file
