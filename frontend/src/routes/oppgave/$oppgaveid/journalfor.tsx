import {createFileRoute} from '@tanstack/react-router'
import {Heading, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getOppgaveOptions} from "@generated/@tanstack/react-query.gen";

export const Route = createFileRoute('/oppgave/$oppgaveid/journalfor')({
  component: JournalforPage,
})

function JournalforPage() {
    const oppgaveId = Route.useParams().oppgaveid;
    const {data: oppgave} = useSuspenseQuery(getOppgaveOptions({path: {oppgaveId: Number(oppgaveId)}}))


    return <VStack gap={"6"}>
        <Heading size="xlarge">Journalfør oppgave {oppgave.oppgaveId}</Heading>

        <pre>
        {JSON.stringify(oppgave, null, 2)}
    </pre>
    </VStack>
}
