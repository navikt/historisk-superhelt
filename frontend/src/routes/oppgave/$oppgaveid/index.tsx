import {createFileRoute, redirect} from '@tanstack/react-router'
import {getOppgaveOptions} from "@generated/@tanstack/react-query.gen";
import {BodyShort, Heading, VStack} from "@navikt/ds-react";

export const Route = createFileRoute('/oppgave/$oppgaveid/')({
    loader: async ({params: {oppgaveid}, context}) => {
        const oppgave = await context.queryClient.ensureQueryData(getOppgaveOptions({path: {oppgaveId: Number(oppgaveid)}}));

        if (oppgave.oppgavetype === "JFR") {
            throw redirect({to: "/oppgave/$oppgaveid/journalfor", params: {oppgaveid}});
        }
        if (oppgave.saksnummer) {
            throw redirect({to: "/sak/$saksnummer/opplysninger", params: {saksnummer: oppgave.saksnummer}});
        }
        return oppgave;


    },
    component: OppgaveComponent
})

function OppgaveComponent() {
    const oppgave = Route.useLoaderData()
    return <VStack gap={"6"}>
        <Heading size="xlarge">Oppgave {oppgave.oppgaveId}</Heading>
        <BodyShort>Vet ikke hvordan denne skal behandles</BodyShort>
        <pre>
        {JSON.stringify(oppgave, null, 2)}
    </pre>
    </VStack>
}
