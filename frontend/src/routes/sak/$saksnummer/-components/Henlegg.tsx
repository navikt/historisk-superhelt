import { henleggSakMutation } from "@generated/@tanstack/react-query.gen";
import { Dialog, Textarea, VStack } from "@navikt/ds-react";
import { BreakpointLg } from "@navikt/ds-tokens/dist/tokens";
import { useMutation, useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import { useState } from "react";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { getOrCreateBrevOptions } from "~/routes/sak/$saksnummer/-api/brev.query";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import { BrevEditor } from "~/routes/sak/$saksnummer/-components/BrevEditor";

interface HenleggProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function Henlegg({ open, onOpenChange }: HenleggProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const hasPermission = sak.rettigheter.includes("HENLEGGE");
    const { data: brev } = useQuery({
        ...getOrCreateBrevOptions(saksnummer, "HENLEGGESEBREV", "BRUKER"),
        enabled: open && hasPermission,
    });
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("");
    const [error, setError] = useState<string | undefined>();

    const henleggMutation = useMutation({
        ...henleggSakMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
            onOpenChange(false);
        },
    });

    const validate = () => {
        if (aarsak.length < 10) {
            setError("Årsak må være minst 10 tegn");
            return false;
        }
        if (aarsak.length > 1000) {
            setError("Årsak kan max være 1000 tegn");
            return false;
        }
        setError(undefined);
        return true;
    };

    const onSubmit = async (brevId: string) => {
        if (!validate()) return;
        await henleggMutation.mutateAsync({
            path: { saksnummer },
            body: {
                henleggelseBrevId: brevId,
                aarsak,
            },
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <Dialog.Popup closeOnOutsideClick={false} style={{ width: BreakpointLg }}>
                <Dialog.Header>
                    <Dialog.Title>Henlegg sak</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                    <VStack gap="space-16">
                        <Textarea
                            label="Årsak til henleggelse"
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}
                        />
                        <BrevEditor
                            sak={sak}
                            brevId={brev?.uuid}
                            buttonText="Henlegg sak"
                            onSuccess={onSubmit}
                            readOnly={!hasPermission}
                        />
                        <ErrorAlert error={henleggMutation.error} />
                    </VStack>
                </Dialog.Body>
            </Dialog.Popup>
        </Dialog>
    );
}
