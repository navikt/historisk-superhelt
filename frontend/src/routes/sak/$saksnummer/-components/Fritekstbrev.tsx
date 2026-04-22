import {sendBrevMutation} from "@generated/@tanstack/react-query.gen";
import {Dialog, ErrorSummary} from "@navikt/ds-react";
import {useMutation, useQuery, useSuspenseQuery} from "@tanstack/react-query";
import {useParams} from "@tanstack/react-router";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import {BrevEditor} from "~/routes/sak/$saksnummer/-components/BrevEditor";

interface FritekstBrevProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function FritekstBrev({ open, onOpenChange }: FritekstBrevProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const kanSendeBrev =
        sak.rettigheter.includes("FRITEKSTBREV") ;
    const { data: brev } = useQuery({
        ...getOrCreateBrevOptions(saksnummer, "FRITEKSTBREV", "BRUKER"),
        enabled: open,
    });
    const invalidateSakQuery = useInvalidateSakQuery();

    const sendBrev = useMutation({
        ...sendBrevMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
            onOpenChange(false);
        },
    });

    const onBrevSend = async (brevId: string) => {
        await sendBrev.mutateAsync({
            path: { saksnummer, brevId },
        });
    };
    const hasError = !!sendBrev.error;

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <Dialog.Popup closeOnOutsideClick={false}>
                <Dialog.Header>
                    <Dialog.Title>Skriv brev til bruker</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                    <BrevEditor
                        sak={sak}
                        brevId={brev?.uuid}
                        buttonText="Send brev"
                        onSuccess={onBrevSend}
                        readOnly={!kanSendeBrev}
                    />
                    {hasError && (
                        <ErrorSummary heading="Sending av brev feilet">
                            <ErrorSummary.Item>{sendBrev.error?.detail ?? "En ukjent feil oppstod"}</ErrorSummary.Item>
                        </ErrorSummary>
                    )}
                </Dialog.Body>
            </Dialog.Popup>
        </Dialog>
    );
}
