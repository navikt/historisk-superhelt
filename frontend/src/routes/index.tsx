import {createFileRoute} from '@tanstack/react-router'
import {OppgaveTabell} from "~/common/oppgave/OppgaveTabell";
import {useSuspenseQuery} from "@tanstack/react-query";
import {hentOppgaverForSaksbehandlerOptions} from "@generated/@tanstack/react-query.gen";
import {Heading, VStack} from "@navikt/ds-react";

export const Route = createFileRoute('/')({
    component: Index,
})

function Index() {
    const {data: oppgaver} = useSuspenseQuery({...hentOppgaverForSaksbehandlerOptions()});
    return <VStack gap={"space-8"}>
        <Heading size={"large"}>Dine oppgaver fra Gosys</Heading>
        <OppgaveTabell oppgaver={oppgaver} dineOppgaver={true}/>
    </VStack>

}