import type {Sak} from "@generated";
import {getSakStatusOptions, retryFeiletUtbetalingMutation} from "@generated/@tanstack/react-query.gen";
import {ArrowCirclepathReverseIcon} from "@navikt/aksel-icons";
import {Button, VStack} from "@navikt/ds-react";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface Props {
    sak: Sak
}

export default function UtbetalingRetryButton({sak}: Props) {
    const {data: sakStatus}=useSuspenseQuery(getSakStatusOptions({path: {saksnummer: sak.saksnummer}}))

    const invalidateSakQuery = useInvalidateSakQuery();
    const retryMutation = useMutation({
        ...retryFeiletUtbetalingMutation()
        , onSuccess: () => {
            invalidateSakQuery(sak.saksnummer);
        }
    })

    function retryUtbetaling() {
        retryMutation.mutate({path: {saksnummer: sak.saksnummer}})
    }

    return <VStack>
        <Button variant="secondary" data-color={"warning"} onClick={retryUtbetaling}
                disabled={sakStatus.utbetalingStatus !== "FEILET"}
                loading={retryMutation.status === "pending"}
                icon={<ArrowCirclepathReverseIcon/>}>Prøv å sende utbetaling på nytt</Button>
        <ErrorAlert error={retryMutation.error}/>
    </VStack>
}