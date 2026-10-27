import { Detail, Heading, Page, Tabs, VStack } from "@navikt/ds-react";
import {
    finnAttesteringsOppgaverForSaksbehandlerOptions,
    hentOppgaverForSaksbehandlerOptions,
} from "@superhelt/api/react-query.gen";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute } from "@tanstack/react-router";
import { OppgaveTabell } from "~/common/oppgave/OppgaveTabell";

export const Route = createFileRoute("/")({
    component: Index,
});

function Index() {
    const { data: saksbehandlersOppgaver } = useSuspenseQuery({ ...hentOppgaverForSaksbehandlerOptions() });
    const { data: attesteringsoppgaver } = useSuspenseQuery({ ...finnAttesteringsOppgaverForSaksbehandlerOptions() });
    return (
        <Page.Block width="2xl" as={VStack} paddingBlock="space-24" gap="space-16">
            <Tabs defaultValue="saksbehandler">
                <Tabs.List>
                    <Tabs.Tab value="saksbehandler" label={`Dine oppgaver (${saksbehandlersOppgaver.length})`} />
                    <Tabs.Tab value="attestering" label={`Attesteringsoppgaver (${attesteringsoppgaver.length})`} />
                </Tabs.List>
                <Tabs.Panel value="saksbehandler">
                    <Heading size="large">Dine oppgaver fra Gosys</Heading>
                    <Detail>
                        Bare oppgaver som kan behandles her vises. Plukk nye oppgaver i gosys om du trenger flere
                    </Detail>
                    <OppgaveTabell oppgaver={saksbehandlersOppgaver} visTilordnet={false} visBruker={true} />
                </Tabs.Panel>
                <Tabs.Panel value="attestering">
                    <Heading size="large">Attesteringsoppgaver du kan plukke</Heading>
                    <Detail>Enhetens ufordelte attesteringsoppgaver som ikke er opprettet av deg</Detail>
                    <OppgaveTabell oppgaver={attesteringsoppgaver} visTilordnet={false} visBruker={true} />
                </Tabs.Panel>
            </Tabs>
        </Page.Block>
    );
}
