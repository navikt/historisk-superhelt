---
name: aksel-component
description: Scaffold en responsiv React-komponent med Aksel Design System og riktige spacing-tokens
---

Du lager en ny React-komponent med Navs Aksel Design System.

## Viktige regler

1. **Aldri bruk Tailwind padding/margin** (`p-*`, `m-*`, `px-*`, `py-*`)
2. **Alltid bruk Aksel spacing-tokens** med `space-`-prefiks
3. **Bruk Aksel-komponenter**: Box, VStack, HGrid, Heading, BodyShort, Button, etc.
4. **Desktop-først responsivitet**: bruk `lg`/`md` som primær, ikke `xs` som base

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
      padding={{ lg: "space-24", md: "space-16" }}
      borderRadius="large"
    >
      <VStack gap="space-4">
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
  padding={{ lg: "space-24", md: "space-16" }}
  borderRadius="large"
  className="hover:shadow-lg transition-shadow"
>
  <VStack gap="space-4">
    <Heading size="medium" level="3">
      {title}
    </Heading>
    <BodyShort>{description}</BodyShort>
  </VStack>
</Box>
```

### Responsiv grid-layout

```tsx
<HGrid columns={{ lg: 3, md: 2 }} gap="space-4">
  {items.map((item) => (
    <Card key={item.id} {...item} />
  ))}
</HGrid>
```

### Form-seksjon

```tsx
<Box paddingBlock="space-24">
    <VStack gap="space-8">
    <Heading size="large" level="2">
      Form Title
    </Heading>
    <VStack gap="space-4">
      <TextField label="Felt 1" />
      <TextField label="Felt 2" />
      <Button>Send inn</Button>
    </VStack>
  </VStack>
</Box>
```

### Dashboard Section

```tsx
<Box background="surface-default" padding={{ lg: "space-24", md: "space-16" }} borderRadius="medium">
  <VStack gap="space-8">
    <HStack justify="space-between" align="center">
      <Heading size="large" level="2">
        Section Title
      </Heading>
      <Button variant="secondary" size="small">
        Action
      </Button>
    </HStack>
    <HGrid columns={{ lg: 4, md: 2 }} gap="space-4">
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
  <Box paddingBlock={{ lg: "space-24", md: "space-16" }} paddingInline={{ lg: "space-40", md: "space-24" }}>
    <VStack gap={{ lg: "space-24", md: "space-16" }}>{/* Page content */}</VStack>
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

```tsx
// Desktop-first approach
padding="space-16"                                      // Default
padding={{ lg: "space-24", md: "space-16" }}           // Desktop + tablet
padding={{ lg: "space-32", md: "space-24", sm: "space-16" }}  // Nedskalering

columns={{ lg: 3, md: 2 }}  // Responsive grid
gap={{ lg: "space-8", md: "space-4" }} // Responsive gap
```

Breakpoints:

- `lg`: 1024px (desktop, primær)
- `md`: 768px (tablet)
- `sm`: 480px (ved behov)
- `xs`: 0px (unngå som base)

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

## Forstå koden

After generating the component, explain:

1. **Komponentvalg** — Why `Box`/`VStack`/`HGrid` instead of raw `<div>` with CSS. What accessibility and maintainability benefits do these provide?
2. **Heading-nivåer** — Why `level` matters for accessibility (screen readers use heading hierarchy for navigation). What breaks with wrong heading levels?

🔴 **Rød sone**: Accessibility markup (heading levels, ARIA attributes, labels) is worth understanding deeply — automated tools only catch ~30% of accessibility issues.

Still gjerne spørsmål om valgene over.
