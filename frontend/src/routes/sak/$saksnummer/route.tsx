import {createFileRoute, Outlet} from '@tanstack/react-router'
import {Box, HGrid, Tabs, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/common/person/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";
import {FilePdfIcon, TasklistIcon} from "@navikt/aksel-icons";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import SakHeading from "~/routes/sak/$saksnummer/-components/SakHeading";
import {StepType} from "~/common/process-menu/StepType";
import {ProcessMenuItem} from "~/common/process-menu/ProcessMenuItem";
import {ProcessMenu} from "~/common/process-menu/ProcessMenu";
import {TilstandStatusType} from "~/routes/sak/$saksnummer/-types/sak.types";
import DokumentViewer from "~/routes/sak/$saksnummer/-components/dokumenter/DokumentViewer";
import SakAlert from "~/routes/sak/$saksnummer/-components/SakAlerts";
import {SaksHistorikkTabell} from "~/routes/sak/$saksnummer/-components/SaksHistorikkTabell";

export const Route = createFileRoute('/sak/$saksnummer')({
    component: SakLayout,
    loader: ({params: {saksnummer}, context}) => {
        context.queryClient.ensureQueryData(getSakOptions(saksnummer))
    },
    errorComponent: ({error}) => {
        return <ErrorAlert error={error}/>
    }
})

function SakLayout() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))


    const calculateStepType = (tilstandResultat: TilstandStatusType): StepType => {
        switch (tilstandResultat) {
            case "IKKE_STARTET":
                return StepType.default;
            case "OK":
                return StepType.success;
            case "VALIDERING_FEILET":
                return StepType.warning;
            default:
                return StepType.default;
        }
    }

    return (
        <>
            <PersonHeader maskertPersonId={sak.maskertPersonIdent}/>
            <SakAlert sak={sak}/>
            <HGrid gap="space-24" columns={{lg: 1, xl: 2}} marginBlock={"space-16"}>
                <VStack gap="space-16">
                    <ProcessMenu>
                        <ProcessMenuItem label={"Opplysninger"} stepType={calculateStepType(sak?.tilstand.opplysninger)}
                                         to={"/sak/$saksnummer/opplysninger"}/>
                        <ProcessMenuItem label={"Brev til bruker"}
                                         stepType={calculateStepType(sak?.tilstand.vedtaksbrevBruker)}
                                         to={"/sak/$saksnummer/vedtaksbrevbruker"}/>
                        <ProcessMenuItem label={"Oppsummering"}
                                         stepType={calculateStepType(sak?.tilstand.oppsummering)}
                                         to={"/sak/$saksnummer/oppsummering"}/>

                    </ProcessMenu>

                    <Outlet/>

                </VStack>
                <VStack gap="space-16">
                    <SakHeading sak={sak}/>
                    <Tabs defaultValue="dokumenter">
                        <Tabs.List>
                            <Tabs.Tab
                                value="dokumenter"
                                label="Dokumenter"
                                icon={<FilePdfIcon aria-hidden/>}
                            />
                            <Tabs.Tab
                                value="historikk"
                                label="Sakshistorikk"
                                icon={<TasklistIcon aria-hidden/>}
                            />

                        </Tabs.List>
                        <Tabs.Panel value="dokumenter">
                            <Box width="100%" height="6rem" padding="space-16">
                                <DokumentViewer saksnummer={saksnummer}/>
                            </Box>
                        </Tabs.Panel>
                        <Tabs.Panel value="historikk">
                            <Box width="100%" height="6rem" padding="space-16">
                                <SaksHistorikkTabell maskertPersonIdent={sak.maskertPersonIdent}/>
                            </Box>
                        </Tabs.Panel>
                    </Tabs>
                </VStack>

            </HGrid>
        </>
    )
}
