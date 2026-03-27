import type { ProblemDetail, Sak } from "@generated";
import { ArrowRightIcon } from "@navikt/aksel-icons";
import { Button, Heading, Skeleton, type SortState, Table, VStack } from "@navikt/ds-react";
import { Link } from "@tanstack/react-router";
import { useState } from "react";
import { isoTilLokal } from "~/common/dato.utils";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { useStonadsTypeNavn } from "~/common/sak/useStonadsTypeNavn";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";

interface SakerTableProps {
    saker: Array<Sak>;
    isPending?: boolean;
    error?: ProblemDetail | null;
    hideSaksbehandler?: boolean;
    hideActions?: boolean;
    openInNewTab?: boolean;
}

type SortKey = keyof Sak;

type ScopedSortState = {
    orderBy: SortKey;
} & SortState;

export function SakerTabell({
    saker,
    isPending,
    error,
    hideSaksbehandler,
    hideActions,
    openInNewTab,
}: SakerTableProps) {
    const getStonadsTypeNavn = useStonadsTypeNavn();
    const defaultDescendingColumns: Array<SortKey> = ["soknadsDato", "tildelingsAar"];

    const [sort, setSort] = useState<ScopedSortState>({
        orderBy: "soknadsDato",
        direction: "descending",
    });

    const handleSort = (sortKey: SortKey) => {
        let direction: SortState["direction"];

        if (sortKey === sort.orderBy) {
            direction = sort.direction === "ascending" ? "descending" : "ascending";
        } else {
            direction = defaultDescendingColumns.includes(sortKey) ? "descending" : "ascending";
        }

        setSort({ orderBy: sortKey, direction });
    };

    function comparator<T>(a: T, b: T, orderBy: keyof T): number {
        if (b[orderBy] == null || b[orderBy] < a[orderBy]) {
            return -1;
        }
        if (b[orderBy] > a[orderBy]) {
            return 1;
        }
        return 0;
    }

    const sortedData = saker.slice().sort((a, b) => {
        return sort.direction === "ascending" ? comparator(b, a, sort.orderBy) : comparator(a, b, sort.orderBy);
    });

    if (error) {
        return <ErrorAlert error={error} />;
    }
    if (isPending) {
        return (
            <VStack gap="space-8">
                <Skeleton variant="text" width="100%" />
                {/* 'as'-prop kan brukes på all typografien vår med Skeleton */}
                <Heading as={Skeleton} size="xlarge" width="100%">
                    Placeholder
                </Heading>
                <div style={{ fontSize: "5rem" }}>
                    <Skeleton variant="text" width="100%" />
                </div>
            </VStack>
        );
    }

    function SortableColumnHeader(props: { children?: React.ReactNode; sortKey: SortKey }) {
        return (
            <Table.ColumnHeader sortKey={props.sortKey} sortable scope="col">
                {props.children}
            </Table.ColumnHeader>
        );
    }

    return (
        <Table sort={sort} onSortChange={(sortKey) => handleSort(sortKey as SortKey)} zebraStripes>
            <Table.Header>
                <Table.Row>
                    <SortableColumnHeader sortKey={"saksnummer"}>Saksnummer</SortableColumnHeader>
                    <SortableColumnHeader sortKey="type">Type</SortableColumnHeader>
                    <SortableColumnHeader sortKey="beskrivelse">Beskrivelse</SortableColumnHeader>
                    <SortableColumnHeader sortKey="vedtaksResultat">Status</SortableColumnHeader>
                    <SortableColumnHeader sortKey="tildelingsAar">Tildelingsår</SortableColumnHeader>
                    <SortableColumnHeader sortKey="belop">Beløp</SortableColumnHeader>
                    <SortableColumnHeader sortKey="soknadsDato">Søknadsdato</SortableColumnHeader>
                    {!hideSaksbehandler && <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>}
                    {!hideActions && <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>}
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {sortedData.map((sak) => (
                    <Table.Row
                        key={sak.saksnummer}
                        style={{ textDecorationLine: sak.status === "FEILREGISTRERT" ? "line-through" : "none" }}
                    >
                        <Table.HeaderCell scope="row">{sak.saksnummer}</Table.HeaderCell>
                        <Table.DataCell>{getStonadsTypeNavn(sak.type)}</Table.DataCell>
                        <Table.DataCell>{sak.beskrivelse}</Table.DataCell>
                        <Table.DataCell>
                            <SakStatus sak={sak} />
                        </Table.DataCell>
                        <Table.DataCell>{sak.tildelingsAar ?? "–"}</Table.DataCell>
                        <Table.DataCell>{sak.belop != null ? `${sak.belop} kr` : "–"}</Table.DataCell>
                        <Table.DataCell>{isoTilLokal(sak.soknadsDato)}</Table.DataCell>
                        {!hideSaksbehandler && <Table.DataCell>{sak.saksbehandler.navn}</Table.DataCell>}
                        {!hideActions && (
                            <Table.DataCell>
                                {openInNewTab ? (
                                    <Button
                                        size="small"
                                        variant="secondary"
                                        as="a"
                                        href={`/sak/${sak.saksnummer}`}
                                        target={`sak-${sak.saksnummer}`}
                                        rel="noopener noreferrer"
                                        icon={<ArrowRightIcon aria-hidden />}
                                        aria-label="Åpne sak"
                                    />
                                ) : (
                                    <Button size="small" variant="secondary" as={Link} to={`/sak/${sak.saksnummer}`}>
                                        Åpne sak
                                    </Button>
                                )}
                            </Table.DataCell>
                        )}
                    </Table.Row>
                ))}
            </Table.Body>
        </Table>
    );
}
