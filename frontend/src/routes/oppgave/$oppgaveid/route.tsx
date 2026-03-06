import { getOppgaveOptions } from "@generated/@tanstack/react-query.gen";
import { FilePdfIcon, TasklistIcon } from "@navikt/aksel-icons";
import { HGrid, Tabs, VStack } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute, Outlet } from "@tanstack/react-router";
import { PdfViewer } from "~/common/pdf/PdfViewer";
import { PersonHeader } from "~/common/person/PersonHeader";
import { SakshistorikkJournalTabell } from "~/routes/oppgave/$oppgaveid/-components/SakshistorikkJournalTabell";

export const Route = createFileRoute("/oppgave/$oppgaveid")({
    component: OppgaveLayout,
});

function OppgaveLayout() {
    const oppgaveId = Route.useParams().oppgaveid;
    const { data: oppgave } = useSuspenseQuery(getOppgaveOptions({ path: { oppgaveId: Number(oppgaveId) } }));
    const journalpostId = oppgave.journalpostId;

    return (
        <>
            <PersonHeader maskertPersonId={oppgave.maskertPersonIdent} />
            <HGrid gap="space-24" columns={{ lg: 1, xl: 2 }} marginBlock={"space-16"}>
                <Outlet />
                <VStack gap="space-16">
                    <Tabs defaultValue="dokumenter">
                        <Tabs.List>
                            <Tabs.Tab value="dokumenter" label="Dokumenter" icon={<FilePdfIcon aria-hidden />} />
                            <Tabs.Tab value="historikk" label="Sakshistorikk" icon={<TasklistIcon aria-hidden />} />
                        </Tabs.List>
                        <Tabs.Panel value="dokumenter">
                            <PdfViewer journalpostId={journalpostId} />
                        </Tabs.Panel>
                        <Tabs.Panel value="historikk">
                            <SakshistorikkJournalTabell maskertPersonIdent={oppgave.maskertPersonIdent} />
                        </Tabs.Panel>
                    </Tabs>
                </VStack>
            </HGrid>
        </>
    );
}
