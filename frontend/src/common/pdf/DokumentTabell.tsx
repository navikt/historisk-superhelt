import type { Journalpost } from "@generated/types.gen";
import {
    ArrowDownRightIcon,
    ChevronDownIcon,
    EnvelopeClosedIcon,
    EnvelopeOpenIcon,
    ExternalLinkIcon,
} from "@navikt/aksel-icons";
import { BodyShort, Button, HStack, Link, type SortState, Table, Tooltip } from "@navikt/ds-react";
import { useState } from "react";
import { isoTilLokalTid } from "../dato.utils";

interface DokumentTabellProps {
    dokumenter: Journalpost[];
    åpneEksternt?: boolean;
    selected?: string | undefined;
    onSelect?: (value: string) => void;
}

interface ScopedSortState extends SortState {
    orderBy: "tittel" | "datoSortering";
}

export function DokumentTabell({ dokumenter, åpneEksternt, selected, onSelect }: DokumentTabellProps) {
    const [collapsed, setCollapsed] = useState(false);
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
            size="small"
            sort={sort}
            onSortChange={(sortKey) => {
                handleSort(sortKey as ScopedSortState["orderBy"]);
            }}
            zebraStripes
        >
            <Table.Header>
                <Table.Row>
                    <Table.HeaderCell>
                        {!åpneEksternt && (
                            <Button
                                size="small"
                                variant="tertiary"
                                data-color="neutral"
                                icon={
                                    <Tooltip content={collapsed ? "Vis alle dokumenter" : "Skjul dokumenter"}>
                                        <ChevronDownIcon
                                            style={{
                                                transform: collapsed ? "rotate(-180deg)" : undefined,
                                                transition: "transform 0.2s",
                                            }}
                                        />
                                    </Tooltip>
                                }
                                onClick={() => setCollapsed((c) => !c)}
                            />
                        )}
                    </Table.HeaderCell>
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
                        const isSelected = selected === dokId;
                        if (collapsed && !isSelected) return null;
                        return (
                            <Table.Row
                                key={dokId}
                                onClick={() => onSelect?.(dokId)}
                                style={åpneEksternt ? undefined : { cursor: "pointer" }}
                            >
                                <Table.DataCell width={48}>
                                    {åpneEksternt ? (
                                        <Button
                                            icon={<ExternalLinkIcon title="Åpne i nytt vindu" />}
                                            variant="tertiary"
                                            data-color="info"
                                            size="small"
                                            as={Link}
                                            href={`/api/journalpost/${encodeURIComponent(jp.journalpostId)}/${encodeURIComponent(d.dokumentInfoId)}`}
                                            target="_blank"
                                        />
                                    ) : (
                                        <Button
                                            size="small"
                                            variant={isSelected ? "primary" : "tertiary"}
                                            data-color="info"
                                            onClick={() => onSelect?.(dokId)}
                                            icon={isSelected ? <EnvelopeOpenIcon /> : <EnvelopeClosedIcon />}
                                        />
                                    )}
                                </Table.DataCell>
                                <Table.DataCell>
                                    <BodyShort size="small" weight={isSelected ? "semibold" : "regular"}>
                                        {d.tittel?.toLocaleLowerCase()?.includes("vedlegg") ? (
                                            <HStack>
                                                <ArrowDownRightIcon fontSize="1.5rem" aria-hidden /> {d.tittel}
                                            </HStack>
                                        ) : (
                                            d.tittel
                                        )}
                                    </BodyShort>
                                </Table.DataCell>
                                <Table.DataCell width={201}>
                                    <BodyShort size="small">{isoTilLokalTid(jp.datoSortering)}</BodyShort>
                                </Table.DataCell>
                            </Table.Row>
                        );
                    }),
                )}
            </Table.Body>
        </Table>
    );
}
