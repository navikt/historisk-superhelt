import {BodyShort, Box, Button, GlobalAlert, Heading, HStack, Table, Tag, VStack} from '@navikt/ds-react'
import {Link as RouterLink, useNavigate} from '@tanstack/react-router'
import {findPersonByFnr as findPerson} from "@generated";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getUserInfoOptions, hentOppgaverForSaksbehandlerOptions} from "@generated/@tanstack/react-query.gen";
import {SakStatusType} from "~/routes/sak/$saksnummer/-types/sak.types";

export function OppgaveTabell() {
    const navigate = useNavigate()
    const {data: user} = useSuspenseQuery({
        ...getUserInfoOptions()
    })

    const {data: oppgaver} = useSuspenseQuery({...hentOppgaverForSaksbehandlerOptions()});

    async function doSearch(fnr: string) {

        const {data, error} = await findPerson({
                body: {fnr: fnr}
            }
        )
        if (error) {
            console.error("Noe gikk galt " + error)
            return
        }

        await navigate({to: "/person/$personid", params: {personid: data?.maskertPersonident!}})

    }



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
    if (user.roles.length === 0) {
        return <Box padding={"space-8"}>
            <GlobalAlert status="warning">
                <GlobalAlert.Header>
                    <GlobalAlert.Title>
                        Manglende tilgang
                    </GlobalAlert.Title>
                </GlobalAlert.Header>
                <GlobalAlert.Content>
                    <p>Din bruker har ikke noen roller i systemet og har derfor ikke tilgang.</p>

                    <p>Vennligst kontakt systemadministrator for å få tildelt roller.</p>
                </GlobalAlert.Content>
            </GlobalAlert>
        </Box>
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
                                        <BodyShort size="small">{oppgave.fnr}</BodyShort>
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
                                        <Button as={RouterLink} size="small" variant="secondary" to={`/person/${oppgave.maskertPersonIdent}` }>
                                            Se person
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