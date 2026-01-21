import {createFileRoute, Outlet} from '@tanstack/react-router'
import {PersonHeader} from "~/common/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getOppgaveOptions} from "@generated/@tanstack/react-query.gen";
import {HGrid, VStack} from "@navikt/ds-react";
import {PdfViewer} from "~/common/pdf/PdfViewer";

export const Route = createFileRoute('/oppgave/$oppgaveid')({
    component: RouteComponent,
})

function RouteComponent() {
    const oppgaveId = Route.useParams().oppgaveid;
    const {data: oppgave} = useSuspenseQuery(getOppgaveOptions({path: {oppgaveId: Number(oppgaveId)}}))
    const journalpostId = oppgave.journalpostId;

    return <>
        <PersonHeader maskertPersonId={oppgave.maskertPersonIdent}/>
        <HGrid gap="space-24" columns={{lg: 1, xl: 2}} marginBlock={"space-16"}>
            <Outlet/>
            <VStack gap="space-16">

             <PdfViewer journalpostId={journalpostId} />


            </VStack>
        </HGrid>
    </>
}
