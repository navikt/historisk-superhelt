import {Button, VStack} from "@navikt/ds-react";
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

    function retryUtbetaling() {
        retryMutation.mutate({path: {saksnummer: sak.saksnummer}})
    }

    return <VStack>
        {/* TODO: Hent utbetalingsstatus via eget endepunkt når det er tilgjengelig i API (sak.error finnes ikke lenger) */}
        <Button variant="secondary" data-color={"warning"} onClick={retryUtbetaling}
                disabled={true}
                loading={retryMutation.status === "pending"}
                icon={<ArrowCirclepathReverseIcon/>}>Prøv å sende utbetaling på nytt</Button>
        <ErrorAlert error={retryMutation.error}/>
    </VStack>
}