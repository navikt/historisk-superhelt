import {createFileRoute, Outlet} from '@tanstack/react-router'
import {Box, Heading, HGrid, Skeleton, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/components/PersonHeader";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";

export const Route = createFileRoute('/sak/$saksnummer')({
    component: SakLayout,
    loader: ({params: {saksnummer}, context}) => {
        context.queryClient.ensureQueryData(getSakOptions(saksnummer))
    }
})

function SakLayout() {
    const {saksnummer} = Route.useParams()
    const {data, isPending, error} = useSuspenseQuery(getSakOptions(saksnummer))

    // if (!data) return null;
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
                    <Heading size="small">Viser pdf</Heading>
                    <embed
                        src="/logo.svg"
                        width="100%"
                        height="1200px"
                        type="application/pdf"
                        title="Embedded PDF Viewer"
                    />
                </VStack>

            </HGrid>
        </>
    )
}
