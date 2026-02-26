import type {Sak} from "@generated";
import {getSakStatusOptions} from "@generated/@tanstack/react-query.gen";
import {InfoCard, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import RetryFerdigstillSakButton from "~/routes/sak/$saksnummer/-components/RetryFerdigstillSakButton";
import UtbetalingRetryButton from "~/routes/sak/$saksnummer/-components/UtbetalingRetryButton";

interface Props {
    sak: Sak
}

export default function SakErrorSummary({sak}: Props) {
    const {data: sakStatus}=useSuspenseQuery(getSakStatusOptions({path: {saksnummer: sak.saksnummer}}))
    const utbetalingError = sakStatus.utbetalingStatus === "FEILET"
    const vedtakBrevError = sakStatus.brevStatus !== "SENDT"

    const hasError = utbetalingError || vedtakBrevError

    if (!hasError) {
        return null
    }

    return <VStack gap="space-16">
        {/*<Heading size={"medium"}>Det har skjedd noe feil med saken som må rettes opp</Heading>*/}

        {utbetalingError && <InfoCard data-color="danger">
            <InfoCard.Header>
                <InfoCard.Title>Utbetaling har feilet</InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
                Ta kontakt med support eller prøv å kjøre det på nytt <UtbetalingRetryButton sak={sak}/>
            </InfoCard.Content>
        </InfoCard>}

        {vedtakBrevError && <InfoCard data-color="danger">
            <InfoCard.Header>
                <InfoCard.Title>Vedtaksbrev ikke sendt</InfoCard.Title>
            </InfoCard.Header>
            <InfoCard.Content>
                Brevet er ikke sendt. Ta kontakt med support eller prøv å kjøre det på nytt
                <RetryFerdigstillSakButton sak={sak}/>
            </InfoCard.Content>
        </InfoCard>}

    </VStack>
}