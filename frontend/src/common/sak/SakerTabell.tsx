import {Button, Heading, Skeleton, Table, VStack} from '@navikt/ds-react'
import {Link} from '@tanstack/react-router'
import {ErrorAlert} from "~/common/error/ErrorAlert";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import {isoTilLokal} from "~/common/dato.utils";
import {ProblemDetail, Sak} from "@generated";


interface SakerTableProps {
    saker: Array<Sak>,
    isPending?: boolean,
    error?: ProblemDetail | null,
    hideSaksbehandler?: boolean,
    hideActions?: boolean,
}

export function SakerTabell({saker, isPending, error, hideSaksbehandler, hideActions}: SakerTableProps) {

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
    return (
        <Table>
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell scope="col">Saksnummer</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Type</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Beskrivelse</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Opprettet</Table.HeaderCell>
                    {!hideSaksbehandler && <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>}
                    {!hideActions && <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>}
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {saker.map((sak) => (
                    <Table.Row key={sak.saksnummer} style={{textDecorationLine:sak.status === "FEILREGISTRERT" ? "line-through" : "none"}}>
                        <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                        <Table.DataCell>{sak.type}</Table.DataCell>
                        <Table.DataCell>{sak.beskrivelse}</Table.DataCell>
                        <Table.DataCell>
                            <SakStatus sak={sak}/>
                        </Table.DataCell>
                        <Table.DataCell>{isoTilLokal(sak.opprettetDato)}</Table.DataCell>
                        {!hideSaksbehandler && <Table.DataCell>{sak.saksbehandler.navn}</Table.DataCell>}
                        {!hideActions && <Table.DataCell>
                            <Button size="small" variant="secondary" as={Link} to={`/sak/${sak.saksnummer}`}>
                                Åpne sak
                            </Button>
                        </Table.DataCell>
                        }
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    )
}

