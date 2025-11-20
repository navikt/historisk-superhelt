import {BodyShort, Box, Button, Heading, HStack, Table, Tag, VStack} from '@navikt/ds-react'
import {useNavigate} from '@tanstack/react-router'
import {findPersonByFnr as findPerson} from "@generated";

export function OppgaveTabell() {
    const navigate = useNavigate()

    async function doSearch(fnr: string) {

        const {data, error} = await findPerson({
                body: {fnr: fnr}
            }
        )
        if (error) {
            console.error("Noe gikk galt " + error)
            return
        }

        await navigate({to: "/person/$personid", params: {personid: data?.maskertPersonident!}})

    }

    // Mock data for oppgaver
    const oppgaver = [
        {
            id: 'OPP001',
            fnr: '28498914510',
            navn: 'Overfølsom Kjendis',
            tema: 'Reiseutgifter',
            oppgavetype: 'Behandle søknad',
            frist: '2024-02-15',
            status: 'Under behandling',
            saksbehandler: 'Anne Hansen'
        },
        {
            id: 'OPP002',
            fnr: '02437832318',
            navn: 'Sjelden Motvind',
            tema: 'Fottøy i ulik størrelse',
            oppgavetype: 'Vurder dokument',
            frist: '2024-02-10',
            status: 'Ny',
            saksbehandler: null
        },
        {
            id: 'OPP003',
            fnr: '28497016101',
            navn: 'Gretten Fart',
            tema: 'Parykk',
            oppgavetype: 'Behandle klage',
            frist: '2024-02-20',
            status: 'Venter på bruker',
            saksbehandler: 'Kari Nilsen'
        }
    ]

    const getStatusVariant = (status: string) => {
        switch (status) {
            case 'Ny':
                return 'info'
            case 'Under behandling':
                return 'warning'
            case 'Venter på bruker':
                return 'neutral'
            case 'Ferdig':
                return 'success'
            default:
                return 'neutral'
        }
    }

    return (
        <VStack gap="6">
            <Heading size="xlarge">Mine oppgaver</Heading>
            <Box padding="6" borderWidth="1" borderRadius="medium">
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
                                        <Button size="small" variant="secondary" onClick={() => doSearch(oppgave.fnr)}>
                                            Se person
                                        </Button>
                                    </HStack>
                                </Table.DataCell>
                            </Table.Row>
                        ))}
                    </Table.Body>
                </Table>
            </Box>
        </VStack>
    )
}