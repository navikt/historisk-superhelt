import {
  Table,
  Heading,
  Button,
  Tag,
  Panel,
  HStack,
  VStack,
  BodyShort
} from '@navikt/ds-react'
import { PersonIcon, FileTextIcon, PencilWritingIcon } from '@navikt/aksel-icons'
import { Link } from '@tanstack/react-router'

export function OppgaveTabell() {
  // Mock data for oppgaver
  const oppgaver = [
    {
      id: 'OPP001',
      fnr: '12345678901',
      navn: 'Ola Nordmann',
      tema: 'Reiseutgifter',
      oppgavetype: 'Behandle søknad',
      frist: '2024-02-15',
      status: 'Under behandling',
      saksbehandler: 'Anne Hansen'
    },
    {
      id: 'OPP002',
      fnr: '10987654321',
      navn: 'Kari Hansen',
      tema: 'Fottøy i ulik størrelse',
      oppgavetype: 'Vurder dokument',
      frist: '2024-02-10',
      status: 'Ny',
      saksbehandler: null
    },
    {
      id: 'OPP003',
      fnr: '11122233344',
      navn: 'Per Olsen',
      tema: 'Parykk',
      oppgavetype: 'Behandle klage',
      frist: '2024-02-20',
      status: 'Venter på bruker',
      saksbehandler: 'Kari Nilsen'
    }
  ]

  const getStatusVariant = (status: string) => {
    switch (status) {
      case 'Ny': return 'info'
      case 'Under behandling': return 'warning'
      case 'Venter på bruker': return 'neutral'
      case 'Ferdig': return 'success'
      default: return 'neutral'
    }
  }

  return (
    <VStack gap="6">
      <Panel border>
        <VStack gap="4">
          <Heading size="large">Oppgaveliste</Heading>

          <HStack gap="4">
            <Button
              as={Link}
              to="/person"
              variant="secondary"
              icon={<PersonIcon />}
              size="small"
            >
              Personside
            </Button>
            <Button
              as={Link}
              to="/sak"
              variant="secondary"
              icon={<FileTextIcon />}
              size="small"
            >
              Behandle sak
            </Button>
            <Button
              as={Link}
              to="/brev"
              variant="secondary"
              icon={<PencilWritingIcon />}
              size="small"
            >
              Skriv brev
            </Button>
          </HStack>
        </VStack>
      </Panel>

      <Panel border>
        <Table>
          <Table.Header>
            <Table.Row>
              <Table.HeaderCell scope="col">Oppgave-ID</Table.HeaderCell>
              <Table.HeaderCell scope="col">Person</Table.HeaderCell>
              <Table.HeaderCell scope="col">Tema</Table.HeaderCell>
              <Table.HeaderCell scope="col">Oppgavetype</Table.HeaderCell>
              <Table.HeaderCell scope="col">Frist</Table.HeaderCell>
              <Table.HeaderCell scope="col">Status</Table.HeaderCell>
              <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>
              <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
            </Table.Row>
          </Table.Header>
          <Table.Body>
            {oppgaver.map((oppgave) => (
              <Table.Row key={oppgave.id}>
                <Table.HeaderCell scope="row">{oppgave.id}</Table.HeaderCell>
                <Table.DataCell>
                  <VStack gap="1">
                    <BodyShort weight="semibold">{oppgave.navn}</BodyShort>
                    <BodyShort size="small">{oppgave.fnr}</BodyShort>
                  </VStack>
                </Table.DataCell>
                <Table.DataCell>{oppgave.tema}</Table.DataCell>
                <Table.DataCell>{oppgave.oppgavetype}</Table.DataCell>
                <Table.DataCell>{oppgave.frist}</Table.DataCell>
                <Table.DataCell>
                  <Tag variant={getStatusVariant(oppgave.status)} size="small">
                    {oppgave.status}
                  </Tag>
                </Table.DataCell>
                <Table.DataCell>{oppgave.saksbehandler || 'Ikke tildelt'}</Table.DataCell>
                <Table.DataCell>
                  <HStack gap="2">
                    <Button size="small" variant="primary" as={Link} to="sak">
                      Behandle
                    </Button>
                  </HStack>
                </Table.DataCell>
              </Table.Row>
            ))}
          </Table.Body>
        </Table>
      </Panel>
    </VStack>
  )
}