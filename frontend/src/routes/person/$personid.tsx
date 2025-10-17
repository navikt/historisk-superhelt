import {createFileRoute} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Table, Tabs, Tag, VStack} from '@navikt/ds-react'
import {ExternalLinkIcon, FileTextIcon} from '@navikt/aksel-icons'
import {PersonHeader} from "~/components/PersonHeader";
import {RfcErrorBoundary} from "~/components/error/RfcErrorBoundary";
import {SakerTable} from "~/components/SakerTable";

export const Route = createFileRoute('/person/$personid')({
    component: PersonPage,
})

function PersonPage() {
    const {personid} = Route.useParams()

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

    return (
        <VStack gap="6">
            <Heading size="xlarge">Personside</Heading>

            <PersonHeader maskertPersonId={personid}/>

            <RfcErrorBoundary>
                <Tabs defaultValue="saker">
                    <Tabs.List>
                        <Tabs.Tab value="saker" label="Saker"/>
                        <Tabs.Tab value="dokumenter" label="Dokumenter"/>
                    </Tabs.List>

                    <Tabs.Panel value="saker">
                        <Box padding="4" borderWidth="1" borderRadius="small">
                            <VStack gap="4">
                                <Heading size="medium">Relevante saker</Heading>

                                <SakerTable person={personid}/>
                            </VStack>
                        </Box>
                    </Tabs.Panel>

                    <Tabs.Panel value="dokumenter">
                        <Box padding="4" borderWidth="1" borderRadius="small">
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
                                                        <Button size="small" variant="tertiary"
                                                                icon={<ExternalLinkIcon/>}>
                                                            Last ned
                                                        </Button>
                                                    </HStack>
                                                </Table.DataCell>
                                            </Table.Row>
                                        ))}
                                    </Table.Body>
                                </Table>
                            </VStack>
                        </Box>
                    </Tabs.Panel>
                </Tabs>
            </RfcErrorBoundary>
        </VStack>
    )
}
