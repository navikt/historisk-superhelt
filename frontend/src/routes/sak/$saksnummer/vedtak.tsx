import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import TotrinnkontrollAction from "~/routes/sak/$saksnummer/-components/TotrinnkontrollAction";
import FerdigstillSakAction from "~/routes/sak/$saksnummer/-components/FerdigstillSakAction";

export const Route = createFileRoute('/sak/$saksnummer/vedtak')({
    component: VedtakPage,
})


function VedtakPage() {

    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))

    function renderAction() {

        switch (sak.status) {
            case "UNDER_BEHANDLING":
                return <TotrinnkontrollAction sak={sak}/>
            case "TIL_ATTESTERING":
                return <FerdigstillSakAction sak={sak}/>
            case "FERDIG":
                break;
        }
    }

    return (
        <VStack gap={"8"}>
            {renderAction()}
        </VStack>)
}


