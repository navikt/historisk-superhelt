import {useSuspenseQuery} from "@tanstack/react-query";
import {findSakerForPersonOptions} from "@generated/@tanstack/react-query.gen";
import {Button, Heading, Skeleton, Table, VStack} from "@navikt/ds-react";
import {isoTilLokal} from "~/common/dato.utils";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import {ArrowRightIcon} from "@navikt/aksel-icons";
import type {ProblemDetail, Sak} from "@generated";


interface SakerTableProps {
    maskertPersonIdent: string

}

export function SakshistorikkSakTabell({maskertPersonIdent}: SakerTableProps) {

    const {data, isPending, error} = useSuspenseQuery(({
        ...findSakerForPersonOptions({query: {maskertPersonId: maskertPersonIdent}})
    }))

    const saker = data.filter(sak => sak.status === "FERDIG")

    return <SakshistorikkSakTabellWithButton saker={saker} isPending={isPending} error={error} hideSaksbehandler={true}/>
}

interface SaksHistorikkTableProps {
    saker: Array<Sak>,
    isPending?: boolean,
    error?: ProblemDetail | null,
    hideSaksbehandler?: boolean,
}

function SakshistorikkSakTabellWithButton({saker, isPending, error, hideSaksbehandler}: SaksHistorikkTableProps) {
    const getStonadsTypeNavn = useStonadsTypeNavn()

    if (error) {
        return <ErrorAlert error={error}/>
    }
    if (isPending) {
        return <VStack gap="space-8">
            <Skeleton variant="text" width="100%"/>
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
                    <Table.HeaderCell scope="col">Tildelingsår</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Beløp</Table.HeaderCell>
                    <Table.HeaderCell scope="col">Opprettet</Table.HeaderCell>
                    {!hideSaksbehandler && <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>}
                    <Table.HeaderCell scope="col" align="right"></Table.HeaderCell>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {saker.map((sak) => (
                    <Table.Row key={sak.saksnummer} style={{textDecorationLine:sak.status === "FEILREGISTRERT" ? "line-through" : "none"}}>
                        <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                        <Table.DataCell>{getStonadsTypeNavn(sak.type)}</Table.DataCell>
                        <Table.DataCell>{sak.beskrivelse}</Table.DataCell>
                        <Table.DataCell>
                            <SakStatus sak={sak}/>
                        </Table.DataCell>
                        <Table.DataCell>{sak.tildelingsAar ?? '–'}</Table.DataCell>
                        <Table.DataCell>{sak.belop != null ? `${sak.belop} kr` : '–'}</Table.DataCell>
                        <Table.DataCell>{isoTilLokal(sak.opprettetDato)}</Table.DataCell>
                        {!hideSaksbehandler && <Table.DataCell>{sak.saksbehandler.navn}</Table.DataCell>}
                        <Table.DataCell align="right">
                            <Button
                                size="small"
                                variant="secondary"
                                as="a"
                                href={`/sak/${sak.saksnummer}`}
                                target={`sak-${sak.saksnummer}`}
                                rel="noopener noreferrer"
                                icon={<ArrowRightIcon aria-hidden />}
                                aria-label="Åpne sak i ny fane"
                            />
                        </Table.DataCell>
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    )
}

