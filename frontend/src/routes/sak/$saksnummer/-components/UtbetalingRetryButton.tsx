import {BodyLong, Button, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import {ArrowCirclepathReverseIcon} from "@navikt/aksel-icons";
import {useMutation} from "@tanstack/react-query";
import {retryFeiletUtbetalingMutation} from "@generated/@tanstack/react-query.gen";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface Props {
    sak: Sak
}

export default function UtbetalingRetryButton({sak}: Props) {
    const invalidateSakQuery = useInvalidateSakQuery();
    const retryMutation = useMutation({
        ...retryFeiletUtbetalingMutation()
        , onSuccess: () => {
            invalidateSakQuery(sak.saksnummer);
        }
    })

    if (!sak.error.utbetalingError) {
        return
    }

    function retryUtbetaling() {
        retryMutation.mutate({path: {saksnummer: sak.saksnummer}})
    }

    return <VStack>
        <BodyLong>Saken har en utbetaling som har feilet. Denne kan prøves på nytt</BodyLong>
        <Button variant="secondary" data-color={"warning"} onClick={retryUtbetaling}
                loading={retryMutation.status === "pending"}
                icon={<ArrowCirclepathReverseIcon/>}>Prøv å sende utbetaling
            på nytt</Button>
        <ErrorAlert error={retryMutation.error}/>
    </VStack>
}