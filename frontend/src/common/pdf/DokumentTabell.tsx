import type { Journalpost } from "@generated/types.gen";
import { ArrowDownRightIcon, ExternalLinkIcon } from "@navikt/aksel-icons";
import { Button, HStack, Link, type SortState, Table, Tooltip } from "@navikt/ds-react";
import { useState } from "react";
import { isoTilLokalTid } from "../dato.utils";

interface DokumentTabellProps {
    dokumenter: Journalpost[];
}

interface ScopedSortState extends SortState {
    orderBy: "tittel" | "datoSortering";
}

export function DokumentTabell({ dokumenter }: DokumentTabellProps) {
    const [sort, setSort] = useState<ScopedSortState>({ orderBy: "datoSortering", direction: "descending" });

    function handleSort(sortKey: ScopedSortState["orderBy"]) {
        setSort((prev) => {
            if (prev.orderBy === sortKey) {
                return { ...prev, direction: prev.direction === "ascending" ? "descending" : "ascending" };
            }
            return { orderBy: sortKey, direction: "ascending" };
        });
    }

    function sorterDokumenter(doks: Journalpost[], sortState: ScopedSortState): Journalpost[] {
        const sorted = [...doks].sort((a, b) => {
            let compareValue = 0;
            const aValue = a[sortState.orderBy] || "";
            const bValue = b[sortState.orderBy] || "";
            compareValue = aValue.localeCompare(bValue);
            return sortState.direction === "ascending" ? compareValue : -compareValue;
        });
        return sorted;
    }

    const sorterteDokumenter = sorterDokumenter(dokumenter, sort);

    return (
        <Table
            sort={sort}
            onSortChange={(sortKey) => {
                handleSort(sortKey as ScopedSortState["orderBy"]);
            }}
            zebraStripes
        >
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell></Table.HeaderCell>
                    <Table.ColumnHeader scope="col" sortKey="tittel" sortable>
                        Tittel
                    </Table.ColumnHeader>
                    <Table.ColumnHeader scope="col" sortKey="datoSortering" sortable>
                        Tidspunkt
                    </Table.ColumnHeader>
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {sorterteDokumenter.map((jp) =>
                    (jp.dokumenter || []).map((d) => {
                        const dokId = `${jp.journalpostId}@${d.dokumentInfoId}`;
                        return (
                            <Table.Row key={dokId}>
                                <Table.DataCell width={48}>
                                    <Tooltip content="Åpne dokument i nytt vindu">
                                        <Button
                                            icon={<ExternalLinkIcon aria-hidden />}
                                            variant="tertiary"
                                            data-color="info"
                                            size="small"
                                            as={Link}
                                            href={`/api/journalpost/${encodeURIComponent(jp.journalpostId)}/${encodeURIComponent(d.dokumentInfoId)}`}
                                            target="_blank"
                                            aria-label="Åpne dokument i nytt vindu"
                                            rel="noopener noreferrer"
                                        />
                                    </Tooltip>
                                </Table.DataCell>
                                <Table.DataCell>
                                    {d.tittel?.toLocaleLowerCase()?.includes("vedlegg") ? (
                                        <HStack>
                                            <ArrowDownRightIcon fontSize="1.5rem" aria-hidden /> {d.tittel}
                                        </HStack>
                                    ) : (
                                        d.tittel
                                    )}
                                </Table.DataCell>
                                <Table.DataCell width={201}>{isoTilLokalTid(jp.datoSortering)}</Table.DataCell>
                            </Table.Row>
                        );
                    }),
                )}
            </Table.Body>
        </Table>
    );
}
