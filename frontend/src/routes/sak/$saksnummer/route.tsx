import {createFileRoute, Outlet} from '@tanstack/react-router'
import {Heading, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/components/PersonHeader";
import {useQuery, useSuspenseQuery} from "@tanstack/react-query";
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
        <VStack gap="6">
            <Heading size="xlarge">Sakside</Heading>

            <PersonHeader maskertPersonId={data.maskertPersonIdent}/>
            <Outlet/>
        </VStack>
    )
}
