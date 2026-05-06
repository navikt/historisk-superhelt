import type { Journalpost } from "@generated/types.gen";
import { ArrowDownRightIcon, ExternalLinkIcon } from "@navikt/aksel-icons";
import { HStack, Link, Pagination, type SortState, Table, Tag, VStack } from "@navikt/ds-react";
import { useState } from "react";
import { isoTilLokalTid } from "~/common/dato.utils";

interface DokumentTabellProps {
    dokumenter: Journalpost[];
}

type DokumentSortKey = "tittel" | "datoSortering" | "saksnummer" | "journalpostType";
type ScopedSortState = { orderBy?: DokumentSortKey } & SortState;

type DokumentRad = {
    dokId: string;
    journalpostId: string;
    dokumentInfoId: string;
    tittel?: string | null;
    journalpostTittel?: string | null;
    datoSortering?: string | null;
    saksnummer?: string | null;
    erVedlegg: boolean;
    journalpostType?: string;
};

const defaultSort: ScopedSortState = { orderBy: "datoSortering", direction: "descending" };

function tilDokumentRader(dokumenter: Journalpost[]): DokumentRad[] {
    return dokumenter.flatMap((jp) =>
        (jp.dokumenter ?? []).map((d) => ({
            dokId: `${jp.journalpostId}@${d.dokumentInfoId}`,
            journalpostId: jp.journalpostId,
            dokumentInfoId: d.dokumentInfoId,
            tittel: d.tittel,
            journalpostTittel: jp.tittel,
            datoSortering: jp.datoSortering,
            saksnummer: jp.sak?.fagsakId,
            erVedlegg: d.tittel?.toLocaleLowerCase().includes("vedlegg") ?? false,
            journalpostType: jp.journalpostType,
        })),
    );
}

function comparator(a: DokumentRad, b: DokumentRad, orderBy: DokumentSortKey): number {
    const aVal = orderBy === "tittel" ? (a.journalpostTittel ?? "") : (a[orderBy] ?? "");
    const bVal = orderBy === "tittel" ? (b.journalpostTittel ?? "") : (b[orderBy] ?? "");
    if (bVal < aVal) return -1;
    if (bVal > aVal) return 1;
    return 0;
}

function tieBreaker(a: DokumentRad, b: DokumentRad): number {
    const aJp = a.journalpostTittel ?? "";
    const bJp = b.journalpostTittel ?? "";
    if (aJp < bJp) return -1;
    if (aJp > bJp) return 1;
    if (!a.erVedlegg && b.erVedlegg) return -1;
    if (a.erVedlegg && !b.erVedlegg) return 1;
    return 0;
}

function journalpostTypeLabel(journalpostType?: string) {
    switch (journalpostType) {
        case "INNGAAENDE":
            return (
                <Tag variant="strong" data-color="brand-blue" size="small">
                    Inngående
                </Tag>
            );
        case "UTGAAENDE":
            return (
                <Tag variant="strong" data-color="brand-beige" size="small">
                    Utgående
                </Tag>
            );
        case "NOTAT":
            return (
                <Tag variant="strong" data-color="brand-magenta" size="small">
                    Notat
                </Tag>
            );
        default:
            return (
                <Tag variant="strong" data-color="neutral" size="small">
                    {journalpostType ?? "Ukjent"}
                </Tag>
            );
    }
}

export function DokumentTabell({ dokumenter }: DokumentTabellProps) {
    const [sort, setSort] = useState<ScopedSortState | undefined>();
    const [page, setPage] = useState(1);
    const raderPerSide = 12;

    const handleSort = (sortKey: string) => {
        const sortering = sort ?? defaultSort;
        const direction: SortState["direction"] =
            sortKey === sortering.orderBy && sortering.direction === "descending" ? "ascending" : "descending";
        setSort({ orderBy: sortKey as DokumentSortKey, direction });
        setPage(1);
    };

    const rader = tilDokumentRader(dokumenter);
    const sortering = sort ?? defaultSort;
    const sorterteRader = rader
        .slice()
        .sort((a, b) => {
            const primary =
                sortering.direction === "ascending"
                    ? comparator(b, a, sortering.orderBy)
                    : comparator(a, b, sortering.orderBy);
            return primary !== 0 ? primary : tieBreaker(a, b);
        })
        .slice((page - 1) * raderPerSide, page * raderPerSide);

    return (
        <VStack gap="space-16">
            <Table sort={sort} onSortChange={handleSort} zebraStripes>
                <Table.Header>
                    <Table.Row>
                        <Table.ColumnHeader scope="col" sortKey="saksnummer" sortable>
                            Saksnr
                        </Table.ColumnHeader>
                        <Table.ColumnHeader scope="col" sortKey="journalpostType" sortable>
                            Inn/Ut
                        </Table.ColumnHeader>
                        <Table.ColumnHeader scope="col" sortKey="tittel" sortable>
                            Tittel
                        </Table.ColumnHeader>
                        <Table.ColumnHeader scope="col" sortKey="datoSortering" sortable>
                            Tidspunkt
                        </Table.ColumnHeader>
                    </Table.Row>
                </Table.Header>
                <Table.Body>
                    {sorterteRader.map((rad) => (
                        <Table.Row key={rad.dokId}>
                            <Table.HeaderCell>{rad.saksnummer}</Table.HeaderCell>
                            <Table.DataCell>{journalpostTypeLabel(rad.journalpostType)}</Table.DataCell>
                            <Table.DataCell>
                                <HStack>
                                    {rad.erVedlegg && <ArrowDownRightIcon fontSize="1.5rem" aria-hidden />}
                                    <Link
                                        href={`/api/journalpost/${encodeURIComponent(rad.journalpostId)}/${encodeURIComponent(rad.dokumentInfoId)}`}
                                        target="_blank"
                                        aria-label="Åpne dokument i nytt vindu"
                                        rel="noopener noreferrer"
                                    >
                                        {rad.tittel}
                                        <ExternalLinkIcon title="Åpne i nytt vindu" />
                                    </Link>
                                </HStack>
                            </Table.DataCell>
                            <Table.DataCell>{isoTilLokalTid(rad.datoSortering)}</Table.DataCell>
                        </Table.Row>
                    ))}
                </Table.Body>
            </Table>
            {rader.length > raderPerSide && (
                <Pagination
                    page={page}
                    onPageChange={setPage}
                    count={Math.ceil(rader.length / raderPerSide)}
                    aria-label="Dokumenttabell paginering"
                />
            )}
        </VStack>
    );
}
