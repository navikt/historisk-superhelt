import { getOppgaveOptions } from "@generated/@tanstack/react-query.gen";
import { FilePdfIcon, TasklistIcon } from "@navikt/aksel-icons";
import { Tabs } from "@navikt/ds-react";
import { useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute, Outlet } from "@tanstack/react-router";
import DeltVisning from "~/common/delt-visning/DeltVisning";
import { PdfViewer } from "~/common/pdf/PdfViewer";
import { PersonHeader } from "~/common/person/PersonHeader";
import { useSakshistorikkAntall } from "~/common/sak/historikk/useSakshistorikkAntall";
import { SakshistorikkJournalTabell } from "~/routes/oppgave/$oppgaveid/-components/SakshistorikkJournalTabell";
import { hentJournalpostMetadataQuery } from "./-api/journalpost.query";

export const Route = createFileRoute("/oppgave/$oppgaveid")({
    component: OppgaveLayout,
    loader: ({ params: { oppgaveid }, context }) => {
        context.queryClient.ensureQueryData(getOppgaveOptions({ path: { oppgaveId: Number(oppgaveid) } }));
    },
});

function OppgaveLayout() {
    const oppgaveId = Route.useParams().oppgaveid;
    const { data: oppgave } = useSuspenseQuery(getOppgaveOptions({ path: { oppgaveId: Number(oppgaveId) } }));
    const journalpostId = oppgave.journalpostId;

    const { data: journalpost, isSuccess: erJournalpostLastet } = useQuery(hentJournalpostMetadataQuery(journalpostId));
    const antallDokumenter = erJournalpostLastet ? (journalpost.dokumenter ?? []).length : undefined;
    const dokumenterLabel = antallDokumenter !== undefined ? `Dokumenter (${antallDokumenter})` : "Dokumenter";

    const { sakshistorikkLabel } = useSakshistorikkAntall(oppgave.maskertPersonIdent, "aapen");

    return (
        <>
            <PersonHeader maskertPersonId={oppgave.maskertPersonIdent} />
            <DeltVisning>
                <DeltVisning.Kolonne>
                    <Outlet />
                </DeltVisning.Kolonne>
                <DeltVisning.Kolonne justerbar>
                    <Tabs defaultValue="dokumenter">
                        <Tabs.List>
                            <Tabs.Tab value="dokumenter" label={dokumenterLabel} icon={<FilePdfIcon aria-hidden />} />
                            <Tabs.Tab
                                value="historikk"
                                label={sakshistorikkLabel}
                                icon={<TasklistIcon aria-hidden />}
                            />
                        </Tabs.List>
                        <Tabs.Panel value="dokumenter">
                            <PdfViewer journalpostId={journalpostId} />
                        </Tabs.Panel>
                        <Tabs.Panel value="historikk">
                            <SakshistorikkJournalTabell maskertPersonIdent={oppgave.maskertPersonIdent} />
                        </Tabs.Panel>
                    </Tabs>
                </DeltVisning.Kolonne>
            </DeltVisning>
        </>
    );
}
