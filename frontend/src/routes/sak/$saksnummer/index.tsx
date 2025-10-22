import {createFileRoute, Outlet, useNavigate} from '@tanstack/react-router'
import {Heading, VStack} from '@navikt/ds-react'
import {PersonHeader} from "~/components/PersonHeader";
import {useQuery, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";

export const Route = createFileRoute('/sak/$saksnummer/')({
    component: SakIndex,
})

function SakIndex() {
    const {saksnummer} = Route.useParams()
    const {data, isPending, error} = useSuspenseQuery(getSakOptions(saksnummer))
    const navigate = useNavigate();

    navigate({to:"/sak/$saksnummer/edit", params: {saksnummer}, search:{"status":data.status} , replace:true});

}
