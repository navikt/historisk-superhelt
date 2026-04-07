import type {InfotrygdHistorikk, ProblemDetail, Sak} from "@generated";
import {Button, Heading, Skeleton, type SortState, Table, Tag, VStack} from "@navikt/ds-react";
import {Link} from "@tanstack/react-router";
import {useState} from "react";
import {isoTilLokal} from "~/common/dato.utils";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";
import {formatertValuta} from "~/common/string.utils";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import type {HistorikkRad, HistorikkSortKey} from "./sakshistorikk.types";
import {infotrygdTilHistorikkRad, sakTilHistorikkRad} from "./sakshistorikk.utils";
import {ArrowRightIcon} from "@navikt/aksel-icons";

interface SakshistorikkKombinertProps {
    saker: Array<Sak>;
    infotrygdHistorikk: Array<InfotrygdHistorikk>;
    isPending?: boolean;
    error?: ProblemDetail | null;
    hideSaksbehandler?: boolean;
    hideActions?: boolean;
    openInNewTab?: boolean;
}

type ScopedSortState = { orderBy?: HistorikkSortKey } & SortState;

export function SakshistorikkKombinertTabell({
    saker,
    infotrygdHistorikk,
    isPending,
    error,
    hideSaksbehandler,
    hideActions,
    openInNewTab,
}: SakshistorikkKombinertProps) {
    const getStonadsTypeNavn = useStonadsTypeNavn();

    const defaultSort: ScopedSortState = {
        orderBy: "dato",
        direction: "descending",
    };

    const [sort, setSort] = useState<ScopedSortState | undefined>();

    const handleSort = (sortKey: HistorikkSortKey) => {
        const sortering = sort ?? defaultSort;
        const direction: SortState["direction"] =
            sortKey === sortering.orderBy && sortering.direction === "descending" ? "ascending" : "descending";
        setSort({ orderBy: sortKey, direction });
    };

    function comparator(a: HistorikkRad, b: HistorikkRad, orderBy: HistorikkSortKey): number {
        const aVal = a[orderBy];
        const bVal = b[orderBy];
        if (bVal == null || bVal < (aVal ?? "")) return -1;
        if (bVal > (aVal ?? "")) return 1;
        return 0;
    }

    const rader: HistorikkRad[] = [
        ...saker.map((sak: Sak) => sakTilHistorikkRad(sak, getStonadsTypeNavn(sak.type))),
        ...infotrygdHistorikk.map((h: InfotrygdHistorikk) => infotrygdTilHistorikkRad(h)),
    ];

    const sorterteRader = rader.slice().sort((a, b) => {
        const sortering = sort ?? defaultSort;
        if (sortering.direction === "ascending") {
            return comparator(b, a, sortering.orderBy);
        }
        return comparator(a, b, sortering.orderBy);
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

    function SortableColumnHeader(props: { children?: React.ReactNode; sortKey: HistorikkSortKey }) {
        return (
            <Table.ColumnHeader sortKey={props.sortKey} sortable scope="col">
                {props.children}
            </Table.ColumnHeader>
        );
    }

    return (
        <Table sort={sort} onSortChange={(key) => handleSort(key as HistorikkSortKey)}>
            <Table.Header>
                <Table.Row>
                    <SortableColumnHeader sortKey="id">Saksnr</SortableColumnHeader>
                    <SortableColumnHeader sortKey="kategori">Kategori</SortableColumnHeader>
                    <Table.ColumnHeader scope="col">Beskrivelse</Table.ColumnHeader>
                    <Table.ColumnHeader scope="col">Status</Table.ColumnHeader>
                    <SortableColumnHeader sortKey="dato">Dato</SortableColumnHeader>
                    <SortableColumnHeader sortKey="belop">Beløp</SortableColumnHeader>
                    {!hideSaksbehandler && <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>}
                    {!hideActions && <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>}
                </Table.Row>
            </Table.Header>
            <Table.Body>
                {sorterteRader.map((rad) => (
                    <Table.Row
                        key={`${rad.kilde}-${rad.id}-${rad.dato}`}
                        style={{
                            textDecorationLine: rad.strekedGjennom ? "line-through" : "none",
                        }}
                    >
                        <Table.HeaderCell scope="row">{rad.sak?.saksnummer}</Table.HeaderCell>
                        <Table.DataCell>{rad.kategori}</Table.DataCell>
                        <Table.DataCell>{rad.beskrivelse ?? "–"}</Table.DataCell>
                        <Table.DataCell>
                            {rad.sak ? (
                                <SakStatus sak={rad.sak} />
                            ) : (
                                <Tag data-color="neutral" variant="outline" size="small">
                                    Infotrygd
                                </Tag>
                            )}
                        </Table.DataCell>
                        <Table.DataCell>{isoTilLokal(rad.dato)}</Table.DataCell>
                        <Table.DataCell>{formatertValuta(rad.belop)}</Table.DataCell>
                        {!hideSaksbehandler && <Table.DataCell>{rad.sak?.saksbehandler.navn}</Table.DataCell>}
                        {!hideActions && rad.sak && (
                            <Table.DataCell>
                                {openInNewTab ? (
                                    <Button
                                        size="small"
                                        variant="secondary"
                                        as="a"
                                        href={`/sak/${rad.sak.saksnummer}`}
                                        target={`sak-${rad.sak.saksnummer}`}
                                        rel="noopener noreferrer"
                                        icon={<ArrowRightIcon aria-hidden />}
                                        aria-label="Åpne sak"
                                    />
                                ) : (
                                    <Button
                                        size="small"
                                        variant="secondary"
                                        as={Link}
                                        to={`/sak/${rad.sak.saksnummer}`}
                                    >
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
