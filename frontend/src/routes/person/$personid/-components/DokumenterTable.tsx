import {Button, HStack, Table, Tag} from '@navikt/ds-react'
import {ExternalLinkIcon, FileTextIcon} from "@navikt/aksel-icons";


export function DokumenterTable() {
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
    )
}

