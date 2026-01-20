import {Box, Button, Heading, HStack, Link, Table, Tag, VStack} from '@navikt/ds-react'
import {Link as RouterLink} from '@tanstack/react-router'
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentOppgaverForSaksbehandlerOptions} from "@generated/@tanstack/react-query.gen";
import {SakStatusType} from "~/routes/sak/$saksnummer/-types/sak.types";

export function SaksbehandlersOppgaveTabell() {
    const {data: oppgaver} = useSuspenseQuery({...hentOppgaverForSaksbehandlerOptions()});

    const getStatusVariant = (status?: SakStatusType) => {
        switch (status) {
            case "UNDER_BEHANDLING":
                return 'info'
            case "TIL_ATTESTERING":
                return 'success'
            case "FERDIG":
                return 'warning'
            default:
                return 'neutral'
        }
    }

    return (
        <VStack gap="6">
            <Heading size="xlarge">Mine oppgaver</Heading>
            <Box padding="6" borderWidth="1" borderRadius="medium">
                <Table>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell scope="col">Oppgave-ID</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Person</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Oppgavetype</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Frist</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Status</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Saksbehandler</Table.HeaderCell>
                            <Table.HeaderCell scope="col">Handlinger</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>
                    <Table.Body>
                        {oppgaver.map((oppgave) => (
                            <Table.Row key={oppgave.oppgaveId}>
                                <Table.HeaderCell scope="row">{oppgave.oppgaveId}</Table.HeaderCell>
                                <Table.DataCell>
                                    <VStack gap="1">
                                        <Link as={ RouterLink} to={`/person/${oppgave.maskertPersonIdent}`}>{oppgave.fnr}</Link>
                                    </VStack>
                                </Table.DataCell>
                                <Table.DataCell>{oppgave.oppgavetype}</Table.DataCell>
                                <Table.DataCell>{oppgave.fristFerdigstillelse}</Table.DataCell>
                                <Table.DataCell>
                                    <Tag variant={getStatusVariant(oppgave.sakStatus)} size="small">
                                        {oppgave.sakStatus?? "-"}
                                    </Tag>
                                </Table.DataCell>
                                <Table.DataCell>{oppgave.tilordnetRessurs || 'Ikke tildelt'}</Table.DataCell>
                                <Table.DataCell>
                                    <HStack gap="2">
                                        <Button as={RouterLink} size="small" variant="secondary" to={`/oppgave/${oppgave.oppgaveId}`} >
                                           Journalfør
                                        </Button>
                                    </HStack>
                                </Table.DataCell>
                            </Table.Row>
                        ))}
                    </Table.Body>
                </Table>
            </Box>
        </VStack>
    )
}