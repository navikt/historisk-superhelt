import {Button, Heading, Skeleton, Table, Tag, VStack} from '@navikt/ds-react'
import {Link} from '@tanstack/react-router'
import {useSuspenseQuery} from "@tanstack/react-query";
import {ErrorAlert} from "~/components/error/ErrorAlert";
import {finnSakerForPersonOptions} from "~/routes/person/$personid/-api/person.query";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";


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


export function SakerTable({person}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(finnSakerForPersonOptions(person))

    if (error) {
        return <ErrorAlert error={error}/>
    }
    if (isPending) {
        return <VStack gap="space-8">
            <Skeleton variant="text" width="100%"/>
            {/* 'as'-prop kan brukes på all typografien vår med Skeleton */}
            <Heading as={Skeleton} size="xlarge" width="100%">
                Placeholder
            </Heading>
            <div style={{fontSize: "5rem"}}>
                <Skeleton variant="text" width="100%"/>
            </div>
        </VStack>
    }
    const saker = data || []
    return (
        <Table>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col">Saksnummer</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Tema</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Tittel</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Opprettet</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {saker.map((sak) => (
                    <Table.Row key={sak.saksnummer}>
                        <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                        <Table.DataCell>{sak.type}</Table.DataCell>
                        <Table.DataCell>{sak.tittel}</Table.DataCell>
                        <Table.DataCell>
                           <SakStatus sak={sak}/>
                        </Table.DataCell>
                        <Table.DataCell>{sak.opprettetDato}</Table.DataCell>
                        <Table.DataCell>{sak.saksbehandler}</Table.DataCell>
                        <Table.DataCell>
                            <Button size="small" variant="secondary" as={Link} to={`/sak/${sak.saksnummer}`}>
                                Åpne sak
                            </Button>
                        </Table.DataCell>
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    )
}

