import {createFileRoute, Outlet} from '@tanstack/react-router'
import {Box, Heading, HGrid, Tabs, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/components/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";
import {FilesIcon, TasklistIcon} from "@navikt/aksel-icons";

export const Route = createFileRoute('/sak/$saksnummer')({
    component: SakLayout,
    loader: ({params: {saksnummer}, context}) => {
        context.queryClient.ensureQueryData(getSakOptions(saksnummer))
    }
})

function SakLayout() {
    const {saksnummer} = Route.useParams()
    const {data, isPending, error} = useSuspenseQuery(getSakOptions(saksnummer))

    // TODO loading and error states
    return (
        <>
            <PersonHeader maskertPersonId={data.maskertPersonIdent}/>
            <HGrid gap="space-24" columns={{lg: 1, xl: 2}}>
                <VStack gap="space-16">
                    <Heading size="large">Sak {saksnummer}</Heading>
                    <Outlet/>
                </VStack>
                <VStack gap="space-16">
                    <Tabs defaultValue="soknad">
                        <Tabs.List>
                            <Tabs.Tab
                                value="soknad"
                                label="Søknad"
                                icon={<FilesIcon aria-hidden/>}
                            />
                            <Tabs.Tab
                                value="inbox"
                                label="Historikk"
                                icon={<TasklistIcon aria-hidden/>}
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
                    </Tabs>

                </VStack>

            </HGrid>
        </>
    )
}
