import { createFileRoute } from '@tanstack/react-router'
import {
  Heading,
  Panel,
  BodyShort,
  Button,
  Textarea,
  RadioGroup,
  Radio,
  Alert,
  Tabs,
  Table,
  Tag,
  VStack,
  HStack
} from '@navikt/ds-react'
import { FileTextIcon, ClockIcon } from '@navikt/aksel-icons'
import { useState } from 'react'

export const Route = createFileRoute('/sak')({
  component: SakPage,
})

function SakPage() {
  const [selectedSak, setSelectedSak] = useState<string>('')
  const [vedtak, setVedtak] = useState<string>('')
  const [begrunnelse, setBegrunnelse] = useState<string>('')

  // Mock data for available cases
  const saker = [
    {
      id: 'SAK001',
      bruker: 'Ola Nordmann (12345678901)',
      tema: 'Dagpenger',
      opprettet: '2024-01-15',
      frist: '2024-02-15',
      status: 'Under behandling'
    },
    {
      id: 'SAK003',
      bruker: 'Kari Hansen (10987654321)',
      tema: 'Arbeidsavklaringspenger',
      opprettet: '2024-02-01',
      frist: '2024-03-01',
      status: 'Venter på bruker'
    }
  ]

  const sakDetaljer = {
    id: 'SAK001',
    bruker: 'Ola Nordmann (12345678901)',
    tema: 'Dagpenger',
    beskrivelse: 'Søknad om dagpenger etter permittering',
    opprettet: '2024-01-15',
    frist: '2024-02-15',
    saksbehandler: 'Anne Hansen',
    dokumenter: [
      {
        tittel: 'Søknad om dagpenger',
        dato: '2024-01-15',
        type: 'Innkommende'
      },
      {
        tittel: 'Inntektsopplysninger fra arbeidsgiver',
        dato: '2024-01-20',
        type: 'Innkommende'
      }
    ],
    historikk: [
      {
        dato: '2024-01-15',
        hendelse: 'Søknad mottatt',
        bruker: 'System'
      },
      {
        dato: '2024-01-16',
        hendelse: 'Sak tildelt saksbehandler',
        bruker: 'Anne Hansen'
      },
      {
        dato: '2024-01-20',
        hendelse: 'Tilleggsopplysninger mottatt',
        bruker: 'System'
      }
    ]
  }

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'Under behandling': return 'warning'
      case 'Venter på bruker': return 'neutral'
      default: return 'neutral'
    }
  }

  const handleFatteVedtak = () => {
    if (!vedtak || !begrunnelse) {
      alert('Vennligst fyll ut alle felter')
      return
    }
    alert(`Vedtak fattet: ${vedtak}`)
    // Here you would normally send to backend
  }

  return (
    <VStack gap="6">
      <Heading size="xlarge">Behandle sak</Heading>

      <HStack gap="6" align="start">
        {/* Saksliste */}
        <VStack gap="4" style={{ minWidth: '300px' }}>
          <Panel border>
            <VStack gap="4">
              <Heading size="medium">Saker til behandling</Heading>
              <VStack gap="2">
                {saker.map((sak) => (
                  <Panel
                    key={sak.id}
                    border
                    style={{
                      cursor: 'pointer',
                      backgroundColor: selectedSak === sak.id ? '#e6f3ff' : 'white'
                    }}
                    onClick={() => setSelectedSak(sak.id)}
                  >
                    <VStack gap="2">
                      <HStack justify="space-between" align="start">
                        <BodyShort weight="semibold">{sak.id}</BodyShort>
                        <Tag variant={getStatusVariant(sak.status)} size="small">
                          {sak.status}
                        </Tag>
                      </HStack>
                      <BodyShort size="small">{sak.bruker}</BodyShort>
                      <BodyShort size="small">{sak.tema}</BodyShort>
                      <HStack gap="1" align="center">
                        <ClockIcon fontSize="1rem" />
                        <BodyShort size="small">Frist: {sak.frist}</BodyShort>
                      </HStack>
                    </VStack>
                  </Panel>
                ))}
              </VStack>
            </VStack>
          </Panel>
        </VStack>

        {/* Sakdetaljer */}
        <VStack gap="6" style={{ flex: 1 }}>
          {selectedSak ? (
            <>
              {/* Saksinfo */}
              <Panel border>
                <HStack gap="4" align="start">
                  <FileTextIcon fontSize="2rem" />
                  <VStack gap="4">
                    <Heading size="medium">{sakDetaljer.id} - {sakDetaljer.tema}</Heading>
                    <HStack gap="8">
                      <VStack gap="1">
                        <BodyShort size="small"><strong>Bruker:</strong> {sakDetaljer.bruker}</BodyShort>
                        <BodyShort size="small"><strong>Opprettet:</strong> {sakDetaljer.opprettet}</BodyShort>
                        <BodyShort size="small"><strong>Frist:</strong> {sakDetaljer.frist}</BodyShort>
                      </VStack>
                      <VStack gap="1">
                        <BodyShort size="small"><strong>Saksbehandler:</strong> {sakDetaljer.saksbehandler}</BodyShort>
                        <BodyShort size="small"><strong>Beskrivelse:</strong> {sakDetaljer.beskrivelse}</BodyShort>
                      </VStack>
                    </HStack>
                  </VStack>
                </HStack>
              </Panel>

              <Tabs defaultValue="dokumenter">
                <Tabs.List>
                  <Tabs.Tab value="dokumenter" label="Dokumenter" />
                  <Tabs.Tab value="historikk" label="Historikk" />
                  <Tabs.Tab value="vedtak" label="Fatte vedtak" />
                </Tabs.List>

                <Tabs.Panel value="dokumenter">
                  <Panel border>
                    <VStack gap="4">
                      <Heading size="small">Saksdokumenter</Heading>
                      <VStack gap="2">
                        {sakDetaljer.dokumenter.map((dokument, index) => (
                          <Panel key={index} border>
                            <HStack justify="space-between" align="center">
                              <VStack gap="1">
                                <BodyShort weight="semibold">{dokument.tittel}</BodyShort>
                                <BodyShort size="small">{dokument.dato}</BodyShort>
                              </VStack>
                              <HStack gap="2" align="center">
                                <Tag variant="info" size="small">{dokument.type}</Tag>
                                <Button size="small" variant="secondary">Åpne</Button>
                              </HStack>
                            </HStack>
                          </Panel>
                        ))}
                      </VStack>
                    </VStack>
                  </Panel>
                </Tabs.Panel>

                <Tabs.Panel value="historikk">
                  <Panel border>
                    <VStack gap="4">
                      <Heading size="small">Sakshistorikk</Heading>
                      <VStack gap="3">
                        {sakDetaljer.historikk.map((hendelse, index) => (
                          <HStack key={index} gap="3" align="start">
                            <div style={{
                              width: '8px',
                              height: '8px',
                              borderRadius: '50%',
                              backgroundColor: '#0067c5',
                              marginTop: '8px'
                            }} />
                            <VStack gap="0">
                              <BodyShort weight="semibold">{hendelse.hendelse}</BodyShort>
                              <BodyShort size="small">{hendelse.dato} - {hendelse.bruker}</BodyShort>
                            </VStack>
                          </HStack>
                        ))}
                      </VStack>
                    </VStack>
                  </Panel>
                </Tabs.Panel>

                <Tabs.Panel value="vedtak">
                  <Panel border>
                    <VStack gap="4">
                      <Heading size="small">Fatte vedtak</Heading>

                      <RadioGroup legend="Vedtak" value={vedtak} onChange={setVedtak}>
                        <Radio value="innvilget">Innvilget</Radio>
                        <Radio value="delvis_innvilget">Delvis innvilget</Radio>
                        <Radio value="avslatt">Avslått</Radio>
                      </RadioGroup>

                      <Textarea
                        label="Begrunnelse"
                        description="Gi en detaljert begrunnelse for vedtaket"
                        value={begrunnelse}
                        onChange={(e) => setBegrunnelse(e.target.value)}
                        minRows={4}
                      />

                      <Alert variant="info">
                        Når du fatter vedtak vil det automatisk sendes brev til bruker.
                      </Alert>

                      <HStack gap="4">
                        <Button
                          variant="primary"
                          onClick={handleFatteVedtak}
                          disabled={!vedtak || !begrunnelse}
                        >
                          Fatte vedtak
                        </Button>
                        <Button variant="secondary">
                          Lagre kladd
                        </Button>
                        <Button variant="tertiary">
                          Send til kvalitetssikring
                        </Button>
                      </HStack>
                    </VStack>
                  </Panel>
                </Tabs.Panel>
              </Tabs>
            </>
          ) : (
            <Panel border>
              <VStack gap="4" align="center" style={{ padding: '4rem' }}>
                <FileTextIcon fontSize="3rem" />
                <Heading size="medium">Velg en sak for å starte behandling</Heading>
                <BodyShort>
                  Velg en sak fra listen til venstre for å se detaljer og behandle saken.
                </BodyShort>
              </VStack>
            </Panel>
          )}
        </VStack>
      </HStack>
    </VStack>
  )
}
