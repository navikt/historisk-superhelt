import {
    BodyShort,
    Detail,
    Label,
    Link,
    List,
    Pagination,
    type SortState,
    Table,
    Tag,
    VStack,
} from '@navikt/ds-react';
import {useState} from 'react'
import {Link as RouterLink} from '@tanstack/react-router'
import {OppgaveActionButton} from './OppgaveActionButton'
import {OppgaveMedSak} from "@generated";
import {isoTilLokal} from "~/common/dato.utils";

type Props = {
    oppgaver: OppgaveMedSak[]
    dineOppgaver?: boolean
}

type ScopedSortState = {
    orderBy: keyof OppgaveMedSak
} & SortState

export function OppgaveTabell({oppgaver, dineOppgaver}: Props) {
    const [sort, setSort] = useState<ScopedSortState | undefined>({
        orderBy: 'fristFerdigstillelse',
        direction: 'ascending',
    })
    const [page, setPage] = useState(1)
    const rowsPerPage = 15

    const handleSort = (sortKey: ScopedSortState['orderBy']) => {
        setSort(
            sort && sortKey === sort.orderBy && sort.direction === 'descending'
                ? undefined
                : {
                    orderBy: sortKey,
                    direction:
                        sort && sortKey === sort.orderBy && sort.direction === 'ascending' ? 'descending' : 'ascending',
                },
        )
    }

    function comparator<T>(a: T, b: T, orderBy: keyof T): number {
        if (b[orderBy] == null || b[orderBy] < a[orderBy]) {
            return -1
        }
        if (b[orderBy] > a[orderBy]) {
            return 1
        }
        return 0
    }

    const sortedData = oppgaver
        .slice()
        .sort((a, b) => {
            if (sort) {
                return sort.direction === 'ascending' ? comparator(b, a, sort.orderBy) : comparator(a, b, sort.orderBy)
            }
            return 1
        })
        .slice((page - 1) * rowsPerPage, page * rowsPerPage)

    return (
        <div>
            <Detail>{`${oppgaver.length} oppgave${oppgaver.length === 1 ? '' : 'r'}`}</Detail>
            <VStack gap="space-16">
                <Table
                    zebraStripes
                    sort={sort}
                    onSortChange={(sortKey) => handleSort(sortKey as ScopedSortState['orderBy'])}
                >
                    <Table.Header>
                        <Table.Row>
                            <Table.DataCell aria-label="Vis mer"/>
                            <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                            <Table.ColumnHeader sortKey="type" sortable>
                                Type
                            </Table.ColumnHeader>
                            <Table.ColumnHeader sortKey="behandlingstema" sortable>
                                Behandlingstema
                            </Table.ColumnHeader>
                            <Table.ColumnHeader sortKey="oppgavestatus" sortable>
                                Status
                            </Table.ColumnHeader>
                            <Table.ColumnHeader sortKey="fristFerdigstillelse" sortable>
                                Frist
                            </Table.ColumnHeader>
                            {dineOppgaver && (
                                <Table.ColumnHeader sortKey="personident" sortable>
                                    Bruker
                                </Table.ColumnHeader>
                            )}
                            {!dineOppgaver && (
                                <Table.ColumnHeader sortKey="tilordnetRessurs" sortable>
                                    Tildelt
                                </Table.ColumnHeader>
                            )}
                            <Table.DataCell aria-label="Journalpost"/>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {sortedData.map((oppgave) => (
                            <Table.ExpandableRow
                                key={`${oppgave.oppgaveId}`}
                                content={<Detaljer oppgave={oppgave}/>}
                            >
                                <Table.DataCell>
                                    <OppgaveActionButton oppgave={oppgave}/>
                                </Table.DataCell>
                                <Table.DataCell>{oppgave.oppgavetype}</Table.DataCell>
                                <Table.DataCell>
                                    {oppgave.oppgaveGjelder}
                                </Table.DataCell>
                                <Table.DataCell>
                                    {oppgave.oppgavestatus || oppgave.sakStatus}
                                </Table.DataCell>
                                <Table.DataCell>{isoTilLokal(oppgave.fristFerdigstillelse)}</Table.DataCell>
                                {dineOppgaver && (
                                    <Table.DataCell>
                                        {' '}
                                        {oppgave.fnr ? (
                                            <Link
                                                as={RouterLink}
                                                to={`/person/${oppgave.maskertPersonIdent}`}
                                                style={{textDecoration: 'none'}}
                                            >
                                                {oppgave.fnr}
                                            </Link>
                                        ) : (
                                            'ukjent'
                                        )}
                                    </Table.DataCell>
                                )}
                                {!dineOppgaver && (
                                    <Table.DataCell>{oppgave.tilordnetRessurs ?? ''}</Table.DataCell>
                                )}
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
    )
}

function Detaljer({oppgave: oppgave}: { oppgave: OppgaveMedSak }) {
    function BehandlendeSystem({oppgave}: { oppgave: OppgaveMedSak }) {
        if (oppgave.saksnummer) {
            return <Tag data-color="success" variant="outline">SuperHelt</Tag>;
        }
        if (oppgave.behandlesAvApplikasjon) {
            return <Tag data-color="warning" variant="strong">{oppgave.behandlesAvApplikasjon}</Tag>;
        }
        if (oppgave.opprettetAv?.startsWith('jfr-infotrygd')) {
            return <Tag data-color="warning" variant="strong">Infotrygd</Tag>;
        }
        return <Tag data-color="neutral" variant="outline">Ukjent</Tag>;
    }

    function Kommentar(props: { line: string }) {
        const split = props.line.split('---\\n')
        const head = split[0]
        const body = split[1]?.replaceAll('\\n', ' ')
        return (
            <List.Item>
                <i>{head}</i> -- {body}
            </List.Item>
        )
    }

    return (
        <VStack gap={"space-20"}>
            <div>
                <Label textColor="subtle">Sak</Label>
                <Link as={RouterLink}
                      to={`/sak/${oppgave.saksnummer}`}
                >
                    <BodyShort>{oppgave.saksnummer}</BodyShort>
                </Link>
            </div>
            <div>
                <Label textColor="subtle">Oppgave id i gosys</Label>
                <BodyShort>{oppgave.oppgaveId}</BodyShort>
            </div>
            <div>
                <Label textColor="subtle">Behandlende system</Label>
                <BodyShort>
                    <BehandlendeSystem oppgave={oppgave}/>
                </BodyShort>
            </div>
            <div>
                <Label textColor="subtle">Kommentarer</Label>
                <List>
                    {oppgave?.beskrivelse
                        ?.split('--- ')
                        .filter((line: string) => line.trim() !== '')
                        .map((line: string) => (
                            <Kommentar key={line} line={line}/>
                        ))}
                </List>
            </div>
        </VStack>
    );
}
