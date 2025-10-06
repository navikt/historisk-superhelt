import {createFileRoute, Link} from '@tanstack/react-router'
import {Button, Heading, HStack, Panel, Table, Tabs, Tag, VStack} from '@navikt/ds-react'
import {ExternalLinkIcon, FileTextIcon} from '@navikt/aksel-icons'
import {PersonHeader} from "~/components/PersonHeader";

export const Route = createFileRoute('/person/$personid')({
  component: PersonPage,
})

function PersonPage() {
  const { personid } = Route.useParams()

  const saker = [
    {
      id: 'SAK001',
      tema: 'Reiseutgifter',
      status: 'Under behandling',
      opprettet: '2024-01-15',
      saksbehandler: 'Anne Hansen'
    },
    {
      id: 'SAK002',
      tema: 'Parykk',
      status: 'Avsluttet',
      opprettet: '2023-11-20',
      saksbehandler: 'Per Olsen'
    },
    {
      id: 'SAK003',
      tema: 'Caps eller lue',
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

      <PersonHeader maskertPersonId={personid}/>

      <Tabs defaultValue="saker">
        <Tabs.List>
          <Tabs.Tab value="saker" label="Saker"/>
          <Tabs.Tab value="dokumenter" label="Dokumenter"/>
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
                          <Button size="small" variant="secondary" as={Link} to={"/sak"}>
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
                            <FileTextIcon/>
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
                            <Button size="small" variant="tertiary" icon={<ExternalLinkIcon/>}>
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
