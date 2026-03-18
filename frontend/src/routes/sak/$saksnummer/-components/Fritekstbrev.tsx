import { sendBrevMutation } from "@generated/@tanstack/react-query.gen";
import { Dialog, ErrorSummary } from "@navikt/ds-react";
import { BreakpointLg } from "@navikt/ds-tokens/dist/tokens";
import { useMutation, useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import { getOrCreateBrevOptions } from "~/routes/sak/$saksnummer/-api/brev.query";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import { BrevEditor } from "~/routes/sak/$saksnummer/-components/BrevEditor";

interface FritekstBrevProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function FritekstBrev({ open, onOpenChange }: FritekstBrevProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE");
    const { data: brev } = useQuery({
        ...getOrCreateBrevOptions(saksnummer, "FRITEKSTBREV", "BRUKER"),
        enabled: open && hasSaksbehandleRettighet,
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
            <Dialog.Popup closeOnOutsideClick={false} style={{ width: BreakpointLg }}>
                <Dialog.Header>
                    <Dialog.Title>Skriv brev til bruker</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                    <BrevEditor
                        sak={sak}
                        brevId={brev?.uuid}
                        buttonText="Send brev"
                        onSuccess={onBrevSend}
                        readOnly={!hasSaksbehandleRettighet}
                    />
                    {hasError && (
                        <ErrorSummary>
                            {sendBrev.error && <ErrorSummary.Item>{sendBrev?.error?.detail}</ErrorSummary.Item>}
                        </ErrorSummary>
                    )}
                </Dialog.Body>
            </Dialog.Popup>
        </Dialog>
    );
}
