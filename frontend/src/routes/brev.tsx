import { createFileRoute } from '@tanstack/react-router'
import {
  Heading,
  Panel,
  BodyShort,
  Button,
  TextField,
  Select,
  Textarea,
  Alert,
  VStack,
  HStack
} from '@navikt/ds-react'
import { PencilWritingIcon, PersonIcon } from '@navikt/aksel-icons'
import { useState } from 'react'

export const Route = createFileRoute('/brev')({
  component: BrevPage,
})

function BrevPage() {
  const [mottaker, setMottaker] = useState<string>('')
  const [brevtype, setBrevtype] = useState<string>('')
  const [tittel, setTittel] = useState<string>('')
  const [innhold, setInnhold] = useState<string>('')

  // Mock data for brevmaler
  const brevmaler = [
    {
      id: 'VEDTAK_INNVILGET',
      navn: 'Vedtak - Innvilget',
      beskrivelse: 'Standard vedtaksbrev for innvilgede søknader'
    },
    {
      id: 'VEDTAK_AVSLATT',
      navn: 'Vedtak - Avslått',
      beskrivelse: 'Standard vedtaksbrev for avslåtte søknader'
    },
    {
      id: 'FORESPØRSEL_OPPLYSNINGER',
      navn: 'Forespørsel om opplysninger',
      beskrivelse: 'Be om tilleggsopplysninger fra bruker'
    },
    {
      id: 'VARSEL_STANS',
      navn: 'Varsel om stans',
      beskrivelse: 'Varsel før ytelse stanses'
    }
  ]

  const handleBrevmalChange = (value: string) => {
    setBrevtype(value)

    // Auto-fill based on template
    const mal = brevmaler.find(m => m.id === value)
    if (mal) {
      setTittel(mal.navn)

      // Set default content based on template
      switch (value) {
        case 'VEDTAK_INNVILGET':
          setInnhold(`Vi viser til din søknad om dagpenger.

Søknaden din er innvilget.

Vedtaket er fattet med hjemmel i folketrygdloven § 4-3.

Begrunnelse:
[Fyll inn begrunnelse]

Du har rett til å klage på dette vedtaket innen 6 uker fra du mottok det.`)
          break
        case 'VEDTAK_AVSLATT':
          setInnhold(`Vi viser til din søknad om dagpenger.

Søknaden din er ikke innvilget.

Vedtaket er fattet med hjemmel i folketrygdloven § 4-3.

Begrunnelse:
[Fyll inn begrunnelse]

Du har rett til å klage på dette vedtaket innen 6 uker fra du mottok det.`)
          break
        case 'FORESPØRSEL_OPPLYSNINGER':
          setInnhold(`Vi trenger flere opplysninger for å behandle saken din.

Vi ber deg sende inn:
- [Spesifiser hvilke dokumenter]

Opplysningene må være sendt til oss innen [dato].

Dersom vi ikke mottar opplysningene innen fristen, vil saken bli avsluttet.`)
          break
        case 'VARSEL_STANS':
          setInnhold(`Vi varsler deg om at utbetalingen av [ytelse] vil bli stanset fra [dato].

Årsak:
[Fyll inn årsak]

Hvis du mener dette er feil, må du ta kontakt med oss innen [dato].`)
          break
        default:
          setInnhold('')
      }
    }
  }

  const handleSendBrev = () => {
    if (!mottaker || !brevtype || !tittel || !innhold) {
      alert('Vennligst fyll ut alle feltene')
      return
    }

    alert('Brev sendt til ' + mottaker)
    // Reset form
    setMottaker('')
    setBrevtype('')
    setTittel('')
    setInnhold('')
  }

  return (
    <VStack gap="6">
      <Heading size="xlarge">Skriv brev</Heading>

      <Panel border>
        <VStack gap="6">
          <HStack gap="4" align="center">
            <PencilWritingIcon fontSize="2rem" />
            <VStack gap="1">
              <Heading size="medium">Opprett nytt brev</Heading>
              <BodyShort>Skriv og send brev til bruker</BodyShort>
            </VStack>
          </HStack>

          <VStack gap="4">
            <HStack gap="4">
              <TextField
                label="Mottaker"
                placeholder="Søk etter person (fødselsnummer eller navn)"
                value={mottaker}
                onChange={(e) => setMottaker(e.target.value)}
                style={{ flex: 1 }}
              />
              <Button variant="secondary" icon={<PersonIcon />}>
                Søk
              </Button>
            </HStack>

            <Select
              label="Brevmal"
              value={brevtype}
              onChange={(e) => handleBrevmalChange(e.target.value)}
            >
              <option value="">Velg brevmal</option>
              {brevmaler.map((mal) => (
                <option key={mal.id} value={mal.id}>
                  {mal.navn}
                </option>
              ))}
            </Select>

            {brevtype && (
              <Alert variant="info">
                {brevmaler.find(m => m.id === brevtype)?.beskrivelse}
              </Alert>
            )}

            <TextField
              label="Tittel"
              value={tittel}
              onChange={(e) => setTittel(e.target.value)}
            />

            <Textarea
              label="Brevinnhold"
              description="Skriv brevteksten her"
              value={innhold}
              onChange={(e) => setInnhold(e.target.value)}
              minRows={10}
            />

            <Alert variant="warning">
              Kontroller at all informasjon er korrekt før du sender brevet.
            </Alert>

            <HStack gap="4">
              <Button
                variant="primary"
                onClick={handleSendBrev}
                disabled={!mottaker || !brevtype || !tittel || !innhold}
              >
                Send brev
              </Button>
              <Button variant="secondary">
                Lagre som kladd
              </Button>
              <Button variant="tertiary">
                Forhåndsvis
              </Button>
            </HStack>
          </VStack>
        </VStack>
      </Panel>

      {/* Nylige brev */}
      <Panel border>
        <VStack gap="4">
          <Heading size="medium">Nylig sendte brev</Heading>
          <VStack gap="2">
            <Panel border>
              <HStack justify="space-between" align="center">
                <VStack gap="1">
                  <BodyShort weight="semibold">Vedtak - Innvilget</BodyShort>
                  <BodyShort size="small">Til: Ola Nordmann (12345678901)</BodyShort>
                  <BodyShort size="small">Sendt: 2024-01-20 14:30</BodyShort>
                </VStack>
                <Button size="small" variant="secondary">
                  Se brev
                </Button>
              </HStack>
            </Panel>

            <Panel border>
              <HStack justify="space-between" align="center">
                <VStack gap="1">
                  <BodyShort weight="semibold">Forespørsel om opplysninger</BodyShort>
                  <BodyShort size="small">Til: Kari Hansen (10987654321)</BodyShort>
                  <BodyShort size="small">Sendt: 2024-01-18 10:15</BodyShort>
                </VStack>
                <Button size="small" variant="secondary">
                  Se brev
                </Button>
              </HStack>
            </Panel>
          </VStack>
        </VStack>
      </Panel>
    </VStack>
  )
}
