import {Button, Heading, Skeleton, Table, VStack} from '@navikt/ds-react'
import {Link} from '@tanstack/react-router'
import {useSuspenseQuery} from "@tanstack/react-query";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import {isoTilLokal} from "~/common/dato.utils";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";


interface SakerTableProps {
    maskertPersonIdent: string
}

export function SakerTable({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}}),
        retry: false,
    }))

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
                    <Table.HeaderCell scope="col">Beskrivelse</Table.HeaderCell>
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
                        <Table.DataCell>{sak.beskrivelse}</Table.DataCell>
                        <Table.DataCell>
                            <SakStatus sak={sak}/>
                        </Table.DataCell>
                        <Table.DataCell>{isoTilLokal(sak.opprettetDato)}</Table.DataCell>
                        <Table.DataCell>{sak.saksbehandler.navn}</Table.DataCell>
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

