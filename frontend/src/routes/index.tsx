import { hentOppgaverForSaksbehandlerOptions } from "@generated/@tanstack/react-query.gen";
import { Detail, Heading, Page, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { OppgaveTabell } from "~/common/oppgave/OppgaveTabell";

export const Route = createFileRoute("/")({
    component: Index,
});

function Index() {
    const { data: oppgaver } = useSuspenseQuery({ ...hentOppgaverForSaksbehandlerOptions() });
    return (
        <Page.Block width="2xl" as={VStack} paddingBlock="space-24" gap="space-16">
            <Heading size="large">Dine oppgaver fra Gosys</Heading>
            <Detail>Bare oppgaver som kan behandles her vises</Detail>
            <OppgaveTabell oppgaver={oppgaver} dineOppgaver={true} />
        </Page.Block>
    );
}
