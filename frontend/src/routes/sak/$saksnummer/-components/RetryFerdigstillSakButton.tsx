import {Button, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import {ArrowCirclepathReverseIcon} from "@navikt/aksel-icons";
import {useMutation} from "@tanstack/react-query";
import {ferdigstillSakMutation} from "@generated/@tanstack/react-query.gen";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface Props {
    sak: Sak
}

export default function RetryFerdigstillSakButton({sak}: Props) {
    const invalidateSakQuery = useInvalidateSakQuery();
    const retryMutation = useMutation({
        ...ferdigstillSakMutation()
        , onSuccess: () => {
            invalidateSakQuery(sak.saksnummer);
        }
    })

    function retryUtbetaling() {
        retryMutation.mutate({path: {saksnummer: sak.saksnummer}})
    }

    return <VStack>
        <Button variant="secondary" data-color={"warning"} onClick={retryUtbetaling}
                disabled={sak.status!== "FERDIG_ATTESTERT"}
                loading={retryMutation.status === "pending"}
                icon={<ArrowCirclepathReverseIcon/>}>Ferdigstill sak</Button>
        <ErrorAlert error={retryMutation.error}/>
    </VStack>
}