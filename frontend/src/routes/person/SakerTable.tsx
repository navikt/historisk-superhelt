import {Button, Heading, Skeleton, Table, Tag, VStack} from '@navikt/ds-react'
import {Link} from '@tanstack/react-router'
import {useQuery} from "@tanstack/react-query";
import {findSakerOptions} from "@api/@tanstack/react-query.gen";
import {ErrorAlert} from "~/components/error/ErrorAlert";
import { ProblemDetail} from "@api";

interface Sak {
    id: string
    tema: string
    status: string
    opprettet: string
    saksbehandler: string
}

interface SakerTableProps {
    person: string
}

const getStatusVariant = (status: string) => {
    switch (status) {
        case 'Under behandling':
            return 'warning'
        case 'Avsluttet':
            return 'success'
        case 'Venter på bruker':
            return 'neutral'
        default:
            return 'neutral'
    }
}

const saker: Array<Sak> = [
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


export function SakerTable({person}: SakerTableProps) {



    const {data, isPending, error} = useQuery( {
        ...findSakerOptions({
            query: {
                personId: person
            },
        }),
        retry: false
    })

    if (error) {
        return <ErrorAlert error={error} problemDetails={error as ProblemDetail}  />
    }
    if (isPending){
        return <VStack gap="space-8">
            <Skeleton variant="text" width="100%" />
            {/* 'as'-prop kan brukes på all typografien vår med Skeleton */}
            <Heading as={Skeleton} size="xlarge" width="100%">
                Placeholder
            </Heading>
            <div style={{ fontSize: "5rem" }}>
                <Skeleton variant="text" width="100%" />
            </div>
        </VStack>
    }
    return (
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
    )
}

