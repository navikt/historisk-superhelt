import {createFileRoute} from '@tanstack/react-router'
import {Heading, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import TotrinnkontrollAction from "~/routes/sak/$saksnummer/-components/TotrinnkontrollAction";
import FerdigstillSakAction from "~/routes/sak/$saksnummer/-components/FerdigstillSakAction";
import SakEndringer from "~/routes/sak/$saksnummer/-components/SakEndringer";
import UtbetalingRetryButton from "~/routes/sak/$saksnummer/-components/UtbetalingRetryButton";

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
                return <FerdigstillSakAction sak={sak}/>
            case "FERDIG_ATTESTERT":
                return <>
                    <Heading size={"medium"}>Saken er ferdig attestert</Heading>
                </>
            case "FERDIG":
                return <>
                    <Heading size={"medium"}>Saken er ferdigstilt</Heading>
                    <UtbetalingRetryButton sak={sak}/>
                </>
        }
    }

    return (
        <VStack gap={"8"}>
            {renderAction()}
            <SakEndringer sak={sak}/>
        </VStack>)
}



