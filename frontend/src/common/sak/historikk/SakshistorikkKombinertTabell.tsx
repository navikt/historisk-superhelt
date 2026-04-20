import type { InfotrygdHistorikk, ProblemDetail, Sak } from "@generated";
import { ExternalLinkIcon } from "@navikt/aksel-icons";
import {
    BodyShort,
    Box,
    Button,
    Heading,
    HGrid,
    Label,
    Skeleton,
    type SortState,
    Table,
    Tag,
    VStack,
} from "@navikt/ds-react";
import { Link } from "@tanstack/react-router";
import { useState } from "react";
import { isoTilLokal } from "~/common/dato.utils";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { useStonadsTypeNavn } from "~/common/sak/useStonadsTypeNavn";
import { formatertValuta } from "~/common/string.utils";
import SakStatus from "~/routes/sak/$saksnummer/-components/SakStatus";
import type { HistorikkRad, HistorikkSortKey } from "./sakshistorikk.types";
import { infotrygdTilHistorikkRad, sakTilHistorikkRad } from "./sakshistorikk.utils";

interface SakshistorikkKombinertProps {
    saker: Array<Sak>;
    infotrygdHistorikk: Array<InfotrygdHistorikk>;
    isPending?: boolean;
    error?: ProblemDetail | null;
    openInNewTab?: boolean;
    size?: "medium" | "large";
}

type ScopedSortState = { orderBy?: HistorikkSortKey } & SortState;

export function SakshistorikkKombinertTabell({
    saker,
    infotrygdHistorikk,
    isPending,
    error,
    openInNewTab,
    size = "large",
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
        ...infotrygdHistorikk.map((h: InfotrygdHistorikk, index) => infotrygdTilHistorikkRad(h, index)),
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

    const renderActionButton = (rad: HistorikkRad) => {
        const sak = rad?.sak;
        if (!sak) {
            return null;
        }
        if (openInNewTab) {
            return (
                <Button
                    size="small"
                    variant="secondary"
                    as="a"
                    href={`/sak/${sak.saksnummer}`}
                    target={`sak-${sak.saksnummer}`}
                    rel="noopener noreferrer"
                    icon={<ExternalLinkIcon aria-hidden />}
                    iconPosition="right"
                    aria-label="Åpne sak"
                >
                    Åpne sak
                </Button>
            );
        }
        return (
            <Button size="small" variant="secondary" as={Link} to={`/sak/${sak.saksnummer}`}>
                Åpne sak
            </Button>
        );
    };

    function SakshistorikkDetaljer(rad: HistorikkRad) {
        return (
            <HGrid gap="space-16" columns={"repeat(auto-fit, minmax(10rem, 1fr))"}>
                <VStack>
                    <Label size="small" textColor="subtle">
                        Beskrivelse
                    </Label>
                    <BodyShort size="small">{rad.beskrivelse ?? "-"}</BodyShort>
                </VStack>

                <VStack>
                    <Label size="small" textColor="subtle">
                        Dato
                    </Label>
                    <BodyShort size="small">{isoTilLokal(rad.dato)}</BodyShort>
                </VStack>
                <VStack>
                    <Label size="small" textColor="subtle">
                        Beløp
                    </Label>
                    <BodyShort size="small">{formatertValuta(rad.belop)}</BodyShort>
                </VStack>
                <VStack>
                    <Label size="small" textColor="subtle">
                        Saksbehandler
                    </Label>
                    <BodyShort size="small">{rad.sak?.saksbehandler.navn ?? "-"}</BodyShort>
                </VStack>
                {rad.sak && (
                    <VStack>
                        <Label size="small" textColor="subtle">
                            Handlinger
                        </Label>
                        {<Box>{renderActionButton(rad)}</Box>}
                    </VStack>
                )}
            </HGrid>
        );
    }

    function MediumHeaderRad() {
        return (
            <Table.Row>
                <Table.ColumnHeader scope="col" aria-label="Vis mer" />
                <SortableColumnHeader sortKey="id">Saksnr</SortableColumnHeader>
                <SortableColumnHeader sortKey="kategori">Kategori</SortableColumnHeader>
                <Table.ColumnHeader scope="col">Status</Table.ColumnHeader>
            </Table.Row>
        );
    }

    function MediumRadDetaljer(rad: HistorikkRad) {
        return (
            <Table.ExpandableRow key={rad.id} content={<SakshistorikkDetaljer {...rad} />}>
                <Table.HeaderCell>{rad.sak?.saksnummer ?? "-"}</Table.HeaderCell>
                <Table.DataCell>
                    <BodyShort>{rad.kategori}</BodyShort>
                </Table.DataCell>
                <Table.DataCell>
                    {rad.sak ? (
                        <SakStatus sak={rad.sak} />
                    ) : (
                        <Tag data-color="neutral" variant="outline" size="small">
                            Infotrygd
                        </Tag>
                    )}
                </Table.DataCell>
            </Table.ExpandableRow>
        );
    }

    return (
        <Table sort={sort} onSortChange={(key) => handleSort(key as HistorikkSortKey)} zebraStripes>
            <Table.Header>
                {size === "medium" ? (
                    <MediumHeaderRad />
                ) : (
                    <Table.Row>
                        <SortableColumnHeader sortKey="id">Saksnr</SortableColumnHeader>
                        <SortableColumnHeader sortKey="kategori">Kategori</SortableColumnHeader>
                        <Table.ColumnHeader scope="col">Beskrivelse</Table.ColumnHeader>
                        <Table.ColumnHeader scope="col">Status</Table.ColumnHeader>
                        <SortableColumnHeader sortKey="dato">Dato</SortableColumnHeader>
                        <SortableColumnHeader sortKey="belop">Beløp</SortableColumnHeader>
                        <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>
                        <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                    </Table.Row>
                )}
            </Table.Header>
            <Table.Body>
                {size === "medium"
                    ? sorterteRader.map((rad) => <MediumRadDetaljer key={rad.id} {...rad} />)
                    : sorterteRader.map((rad) => (
                          <Table.Row
                              key={rad.id}
                              style={{
                                  textDecorationLine: rad.strekedGjennom ? "line-through" : "none",
                              }}
                          >
                              <Table.HeaderCell scope="row">{rad.sak?.saksnummer ?? "-"}</Table.HeaderCell>
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
                              <Table.DataCell>{rad.sak?.saksbehandler.navn}</Table.DataCell>
                              <Table.DataCell>{renderActionButton(rad)}</Table.DataCell>
                          </Table.Row>
                      ))}
            </Table.Body>
        </Table>
    );
}
