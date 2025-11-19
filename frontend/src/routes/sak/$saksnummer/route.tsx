import {createFileRoute, Outlet} from '@tanstack/react-router'
import {Box, HGrid, Tabs, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/components/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";
import {FilePdfIcon, FilesIcon, TasklistIcon} from "@navikt/aksel-icons";
import {ErrorAlert} from "~/components/error/ErrorAlert";
import SakHeading from "~/routes/sak/$saksnummer/-components/SakHeading";
import {StepType} from "~/components/ProcessMenu/StepType";
import {ProcessMenuItem} from "~/components/ProcessMenu/ProcessMenuItem";
import {ProcessMenu} from "~/components/ProcessMenu/ProcessMenu";

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

    return (
        <>
            <PersonHeader maskertPersonId={sak.maskertPersonIdent}/>
            <HGrid gap="space-24" columns={{lg: 1, xl: 2}} marginBlock={"space-16"}>
                <VStack gap="space-16">

                    <ProcessMenu>
                        <ProcessMenuItem label={"Opplysninger"} stepType={StepType.success}
                                         to={"/sak/$saksnummer/soknad"}/>
                        <ProcessMenuItem label={"Vedtaksbrev"} stepType={StepType.warning}
                                         to={"/sak/$saksnummer/brev"}/>
                        <ProcessMenuItem label={"Brev til samhandler"} stepType={StepType.default}
                                         to={"/sak/$saksnummer/brev"}/>

                    </ProcessMenu>

                    <Outlet/>

                </VStack>
                <VStack gap="space-16">
                    <SakHeading sak={sak}/>
                    <Tabs defaultValue="soknad">
                        <Tabs.List>
                            <Tabs.Tab
                                value="soknad"
                                label="Søknad"
                                icon={<FilePdfIcon aria-hidden/>}
                            />
                            <Tabs.Tab
                                value="inbox"
                                label="Historikk"
                                icon={<TasklistIcon aria-hidden/>}
                            />
                            <Tabs.Tab
                                value="dokumenter"
                                label="Dokumenter"
                                icon={<FilesIcon aria-hidden/>}
                            />
                        </Tabs.List>
                        <Tabs.Panel value="soknad">
                            <Box width="100%" height="6rem" padding="space-16">
                                <embed
                                    src="/soknad.pdf"
                                    width="100%"
                                    height="1200px"
                                    type="application/pdf"
                                    title="Embedded PDF Viewer"
                                />
                            </Box>
                        </Tabs.Panel>
                        <Tabs.Panel value="inbox">
                            <Box width="100%" height="6rem" padding="space-16">
                                Her kommer det kanskje litt historikk på tidligere saker?
                            </Box>
                        </Tabs.Panel>
                        <Tabs.Panel value="dokumenter">
                            <Box width="100%" height="6rem" padding="space-16">
                                Her kommer det kanskje dokumenter fra joark?
                            </Box>
                        </Tabs.Panel>
                    </Tabs>

                </VStack>

            </HGrid>
        </>
    )
}
