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

type ScopedSortState = {
    orderBy: keyof Sak;
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
    const defaultDescendingColumns: Array<ScopedSortState["orderBy"]> = ["opprettetDato", "tildelingsAar"];

    const [sort, setSort] = useState<ScopedSortState>({
        orderBy: "opprettetDato",
        direction: "descending",
    });

    const handleSort = (sortKey: ScopedSortState["orderBy"]) => {
        setSort({
            orderBy: sortKey,
            direction:
                sortKey === sort.orderBy
                    ? sort.direction === "ascending"
                        ? "descending"
                        : "ascending"
                    : defaultDescendingColumns.includes(sortKey)
                      ? "descending"
                      : "ascending",
        });
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

    return (
        <Table sort={sort} onSortChange={(sortKey) => handleSort(sortKey as ScopedSortState["orderBy"])}>
            <Table.Header>
                <Table.Row>
                    <Table.ColumnHeader sortKey="saksnummer" sortable>
                        Saksnummer
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="type" sortable>
                        Type
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="beskrivelse" sortable>
                        Beskrivelse
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="status" sortable>
                        Status
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="tildelingsAar" sortable>
                        Tildelingsår
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="belop" sortable>
                        Beløp
                    </Table.ColumnHeader>
                    <Table.ColumnHeader sortKey="opprettetDato" sortable>
                        Opprettet
                    </Table.ColumnHeader>
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
                        <Table.DataCell>{isoTilLokal(sak.opprettetDato)}</Table.DataCell>
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
