import type {OppgaveMedSak} from "@generated";
import {getUserInfoOptions} from "@generated/@tanstack/react-query.gen";
import {BodyShort, HStack, Link, Pagination, type SortState, Table, Tag, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {Link as RouterLink} from "@tanstack/react-router";
import {useState} from "react";
import {isoTilLokal} from "~/common/dato.utils";
import {OppgaveDetaljer} from "~/common/oppgave/OppgaveDetaljer";
import {SakStatusTag} from "~/common/sak/SakStatusTag";
import {useStonadsTypeNavn} from "~/common/sak/useStonadsTypeNavn";
import {OppgaveActionButton} from "./OppgaveActionButton";

type Props = {
    oppgaver: OppgaveMedSak[];
    dineOppgaver?: boolean;
};

type ScopedSortState = {
    orderBy: keyof OppgaveMedSak;
} & SortState;

export function OppgaveTabell({oppgaver, dineOppgaver}: Props) {
    const [sort, setSort] = useState<ScopedSortState | undefined>({
        orderBy: "fristFerdigstillelse",
        direction: "ascending",
    });
    const {data: saksbehandler} = useSuspenseQuery(getUserInfoOptions());
    const stonadsTypeNavn = useStonadsTypeNavn();

    const [page, setPage] = useState(1);
    const rowsPerPage = 15;

    const handleSort = (sortKey: ScopedSortState["orderBy"]) => {
        setSort(
            sort && sortKey === sort.orderBy && sort.direction === "descending"
                ? undefined
                : {
                    orderBy: sortKey,
                    direction:
                        sort && sortKey === sort.orderBy && sort.direction === "ascending"
                            ? "descending"
                            : "ascending",
                },
        );
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

    const sortedData = oppgaver
        .slice()
        .sort((a, b) => {
            if (sort) {
                return sort.direction === "ascending" ? comparator(b, a, sort.orderBy) : comparator(a, b, sort.orderBy);
            }
            return 1;
        })
        .slice((page - 1) * rowsPerPage, page * rowsPerPage);

    const renderSakLink = (saksnummer?: string) => {
        if (!saksnummer) {
            return null;
        }
        return (
            <Link as={RouterLink} to={`/sak/${saksnummer}`}>
                {saksnummer}
            </Link>
        );
    };
    const getKategori = (oppgave: OppgaveMedSak) => {
        if (oppgave.oppgavetype === "JFR") {
            return oppgave.oppgaveGjelderTekst;
        }
        return oppgave.stonadsType
            ? stonadsTypeNavn(oppgave.stonadsType)
            : (oppgave.oppgaveGjelderTekst);
    }
    const renderOppgaveText = (oppgave: OppgaveMedSak) => {
        return (
            <HStack gap={"space-8"}>
                <Tag variant="moderate" data-color="neutral" size="small">
                    {oppgave.oppgaveTypeTekst}
                </Tag>
                <Tag variant="outline" data-color={"info"} size="small">
                    {getKategori(oppgave)}
                </Tag>
                <BodyShort truncate style={{maxWidth: "20rem"}}>
                    {oppgave.sakBeskrivelse}
                </BodyShort>
            </HStack>
        );
    };
    return (
        <div>
            <VStack gap="space-16">
                <Table
                    sort={sort}
                    onSortChange={(sortKey) => handleSort(sortKey as ScopedSortState["orderBy"])}
                    zebraStripes
                >
                    <Table.Header>
                        <Table.Row>
                            <Table.DataCell aria-label="Vis mer"/>

                            <Table.ColumnHeader sortKey="saksnummer" sortable>
                                Saksnummer
                            </Table.ColumnHeader>
                            <Table.ColumnHeader>Hva</Table.ColumnHeader>
                            <Table.ColumnHeader>Sakstatus</Table.ColumnHeader>
                            <Table.ColumnHeader sortKey="fristFerdigstillelse" sortable>
                                Frist
                            </Table.ColumnHeader>
                            {dineOppgaver && (
                                <Table.ColumnHeader sortKey="personident" sortable>
                                    Bruker
                                </Table.ColumnHeader>
                            )}
                            {!dineOppgaver && <Table.ColumnHeader>Tildelt</Table.ColumnHeader>}
                            <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {sortedData.map((oppgave) => (
                            <Table.ExpandableRow
                                key={`${oppgave.oppgaveId}`}
                                content={<OppgaveDetaljer oppgave={oppgave}/>}
                            >
                                <Table.DataCell>{renderSakLink(oppgave.saksnummer)}</Table.DataCell>
                                <Table.DataCell>{renderOppgaveText(oppgave)}</Table.DataCell>
                                <Table.DataCell>
                                    {oppgave.sakStatus && <SakStatusTag status={oppgave.sakStatus}/>}
                                </Table.DataCell>
                                <Table.DataCell>{isoTilLokal(oppgave.fristFerdigstillelse)}</Table.DataCell>
                                {dineOppgaver && (
                                    <Table.DataCell>
                                        {oppgave.fnr ? (
                                            <Link
                                                as={RouterLink}
                                                to={`/person/${oppgave.maskertPersonIdent}`}
                                                style={{textDecoration: "none"}}
                                            >
                                                {oppgave.fnr}
                                            </Link>
                                        ) : (
                                            "ukjent"
                                        )}
                                    </Table.DataCell>
                                )}
                                {!dineOppgaver && <Table.DataCell>{oppgave.tilordnetRessurs ?? ""}</Table.DataCell>}
                                <Table.DataCell>
                                    <OppgaveActionButton oppgave={oppgave} saksbehandlerIdent={saksbehandler.ident}/>
                                </Table.DataCell>
                            </Table.ExpandableRow>
                        ))}
                    </Table.Body>
                </Table>
                {oppgaver.length > rowsPerPage && (
                    <Pagination
                        page={page}
                        onPageChange={setPage}
                        count={Math.ceil(oppgaver.length / rowsPerPage)}
                        size="small"
                    />
                )}
            </VStack>
        </div>
    );
}
