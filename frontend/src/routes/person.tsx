import { createFileRoute } from '@tanstack/react-router'
import {
  Heading,
  Panel,
  BodyShort,
  Table,
  Tabs,
  Button,
  Tag,
  VStack,
  HStack
} from '@navikt/ds-react'
import { PersonIcon, FileTextIcon, ExternalLinkIcon } from '@navikt/aksel-icons'

export const Route = createFileRoute('/person')({
  component: PersonPage,
})

function PersonPage() {
  // Mock data
  const person = {
    fnr: '12345678901',
    navn: 'Ola Nordmann',
    adresse: 'Storgata 1, 0123 Oslo',
    telefon: '12345678',
    epost: 'ola.nordmann@example.com',
    sivilstand: 'Gift',
    barn: 2
  }

  const saker = [
    {
      id: 'SAK001',
      tema: 'Dagpenger',
      status: 'Under behandling',
      opprettet: '2024-01-15',
      saksbehandler: 'Anne Hansen'
    },
    {
      id: 'SAK002',
      tema: 'Sykepenger',
      status: 'Avsluttet',
      opprettet: '2023-11-20',
      saksbehandler: 'Per Olsen'
    },
    {
      id: 'SAK003',
      tema: 'Arbeidsavklaringspenger',
      status: 'Venter på bruker',
      opprettet: '2024-02-01',
      saksbehandler: 'Kari Nilsen'
    }
  ]

  const dokumenter = [
    {
      id: 'DOK001',
      tittel: 'Søknad om dagpenger',
      type: 'Innkommende',
      dato: '2024-01-15',
      sakId: 'SAK001'
    },
    {
      id: 'DOK002',
      tittel: 'Vedtak - Sykepenger',
      type: 'Utgående',
      dato: '2023-12-01',
      sakId: 'SAK002'
    },
    {
      id: 'DOK003',
      tittel: 'Forespørsel om tilleggsopplysninger',
      type: 'Utgående',
      dato: '2024-02-05',
      sakId: 'SAK003'
    }
  ]

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'Under behandling': return 'warning'
      case 'Avsluttet': return 'success'
      case 'Venter på bruker': return 'neutral'
      default: return 'neutral'
    }
  }

  return (
    <VStack gap="6">
      <Heading size="xlarge">Personside</Heading>

      {/* Personinformasjon */}
      <Panel border>
        <HStack gap="4" align="start">
          <PersonIcon fontSize="3rem" />
          <VStack gap="4">
            <Heading size="medium">{person.navn}</Heading>
            <HStack gap="8">
              <VStack gap="1">
                <BodyShort size="small"><strong>Fødselsnummer:</strong> {person.fnr}</BodyShort>
                <BodyShort size="small"><strong>Adresse:</strong> {person.adresse}</BodyShort>
                <BodyShort size="small"><strong>Telefon:</strong> {person.telefon}</BodyShort>
              </VStack>
              <VStack gap="1">
                <BodyShort size="small"><strong>E-post:</strong> {person.epost}</BodyShort>
                <BodyShort size="small"><strong>Sivilstand:</strong> {person.sivilstand}</BodyShort>
                <BodyShort size="small"><strong>Antall barn:</strong> {person.barn}</BodyShort>
              </VStack>
            </HStack>
          </VStack>
        </HStack>
      </Panel>

      <Tabs defaultValue="saker">
        <Tabs.List>
          <Tabs.Tab value="saker" label="Saker" />
          <Tabs.Tab value="dokumenter" label="Dokumenter" />
        </Tabs.List>

        <Tabs.Panel value="saker">
          <Panel border>
            <VStack gap="4">
              <Heading size="medium">Relevante saker</Heading>
              <Table>
                <Table.Header>
                  <Table.Row>
                    <Table.HeaderCell scope="col">Saksnummer</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Tema</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Opprettet</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                  </Table.Row>
                </Table.Header>
                <Table.Body>
                  {saker.map((sak) => (
                    <Table.Row key={sak.id}>
                      <Table.HeaderCell scope="row">{sak.id}</Table.HeaderCell>
                      <Table.DataCell>{sak.tema}</Table.DataCell>
                      <Table.DataCell>
                        <Tag variant={getStatusVariant(sak.status)} size="small">
                          {sak.status}
                        </Tag>
                      </Table.DataCell>
                      <Table.DataCell>{sak.opprettet}</Table.DataCell>
                      <Table.DataCell>{sak.saksbehandler}</Table.DataCell>
                      <Table.DataCell>
                        <Button size="small" variant="secondary">
                          Åpne sak
                        </Button>
                      </Table.DataCell>
                    </Table.Row>
                  ))}
                </Table.Body>
              </Table>
            </VStack>
          </Panel>
        </Tabs.Panel>

        <Tabs.Panel value="dokumenter">
          <Panel border>
            <VStack gap="4">
              <Heading size="medium">Dokumenter</Heading>
              <Table>
                <Table.Header>
                  <Table.Row>
                    <Table.HeaderCell scope="col">Tittel</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Type</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Dato</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Tilknyttet sak</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                  </Table.Row>
                </Table.Header>
                <Table.Body>
                  {dokumenter.map((dokument) => (
                    <Table.Row key={dokument.id}>
                      <Table.DataCell>
                        <HStack gap="2" align="center">
                          <FileTextIcon />
                          <span>{dokument.tittel}</span>
                        </HStack>
                      </Table.DataCell>
                      <Table.DataCell>
                        <Tag
                          variant={dokument.type === 'Innkommende' ? 'info' : 'neutral'}
                          size="small"
                        >
                          {dokument.type}
                        </Tag>
                      </Table.DataCell>
                      <Table.DataCell>{dokument.dato}</Table.DataCell>
                      <Table.DataCell>{dokument.sakId}</Table.DataCell>
                      <Table.DataCell>
                        <HStack gap="2">
                          <Button size="small" variant="secondary">
                            Vis
                          </Button>
                          <Button size="small" variant="tertiary" icon={<ExternalLinkIcon />}>
                            Last ned
                          </Button>
                        </HStack>
                      </Table.DataCell>
                    </Table.Row>
                  ))}
                </Table.Body>
              </Table>
            </VStack>
          </Panel>
        </Tabs.Panel>
      </Tabs>
    </VStack>
  )
}
