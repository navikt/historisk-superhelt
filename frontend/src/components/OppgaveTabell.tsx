import {Table} from "@navikt/ds-react";

export function OppgaveTabell() {
    return <Table>
        <Table.Header>
            <Table.Row>
                <Table.HeaderCell scope="col">Oppgave</Table.HeaderCell>
                <Table.HeaderCell scope="col">Type</Table.HeaderCell>
                <Table.HeaderCell scope="col">Tildelt</Table.HeaderCell>
            </Table.Row>
        </Table.Header>
        <Table.Body>

            <Table.Row>
                <Table.HeaderCell scope="row">123123</Table.HeaderCell>
                <Table.DataCell>Journalføring</Table.DataCell>
                <Table.DataCell>deg</Table.DataCell>
            </Table.Row>

        </Table.Body>
    </Table>;
}