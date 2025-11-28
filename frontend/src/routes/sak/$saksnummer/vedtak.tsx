import {createFileRoute} from '@tanstack/react-router'
import {BodyLong, VStack} from "@navikt/ds-react";
import SakActionButton from "~/routes/sak/$saksnummer/-components/SakActionButton";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";

export const Route = createFileRoute('/sak/$saksnummer/vedtak')({
    component: VedtakPage,
})


function VedtakPage() {

    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))

    return (
        <VStack gap={"8"}>
            <BodyLong>
                Her kan du kommer en oppsummering av saken og mulighet til å fatte vedtak, sende til totrinn osv
            </BodyLong>
            <SakActionButton sak={sak}/>
        </VStack>)
}


