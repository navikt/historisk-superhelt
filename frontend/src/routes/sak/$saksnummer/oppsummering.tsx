import {createFileRoute} from '@tanstack/react-router'
import {Heading, InfoCard, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import TotrinnkontrollAction from "~/routes/sak/$saksnummer/-components/TotrinnkontrollAction";
import AttesterSakAction from "~/routes/sak/$saksnummer/-components/AttesterSakAction";
import SakEndringer from "~/routes/sak/$saksnummer/-components/SakEndringer";
import SakErrorSummary from "~/routes/sak/$saksnummer/-components/SakErrorSummary";

export const Route = createFileRoute('/sak/$saksnummer/oppsummering')({
    component: OppsummeringPage,
})

function OppsummeringPage() {

    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))

    function renderAction() {

        switch (sak.status) {
            case "UNDER_BEHANDLING":
                return <TotrinnkontrollAction sak={sak}/>
            case "TIL_ATTESTERING":
                return <AttesterSakAction sak={sak}/>
            case "FERDIG_ATTESTERT":
                return <>
                    <InfoCard data-color={"warning"}>
                        <InfoCard.Header>
                            <InfoCard.Title>Saken er ferdig attestert men ikke fullført</InfoCard.Title>
                        </InfoCard.Header>
                        <InfoCard.Content>
                            Denne statusen bør være midlertidig. Ta kontakt med support hvis saken forblir i denne
                            statusen over lengre tid.
                        </InfoCard.Content>
                    </InfoCard>
                    <SakErrorSummary sak={sak}/>
                </>
            case "FERDIG":
                return <>
                    <Heading size={"medium"}>Saken er ferdigstilt</Heading>
                    <SakErrorSummary sak={sak}/>
                </>
        }
    }

    return (
        <VStack gap={"8"}>
            {renderAction()}
            <SakEndringer sak={sak}/>
        </VStack>)
}



