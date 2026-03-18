import { gjenapneSakMutation } from "@generated/@tanstack/react-query.gen";
import { BodyLong, Button, Dialog, Textarea, VStack } from "@navikt/ds-react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import { useState } from "react";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface GjenapneProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function Gjenapne({ open, onOpenChange }: GjenapneProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("");
    const [error, setError] = useState<string | undefined>();

    const gjenapne = useMutation({
        ...gjenapneSakMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
            onOpenChange(false);
        },
    });

    const hasPermission = sak.rettigheter.includes("GJENAPNE");

    const validate = () => {
        if (aarsak.length < 5) {
            setError("Årsak må være minst 5 tegn");
            return false;
        }
        if (aarsak.length > 1000) {
            setError("Årsak kan max være 1000 tegn");
            return false;
        }
        setError(undefined);
        return true;
    };

    const onGjenopprett = async () => {
        if (!validate()) return;
        await gjenapne.mutateAsync({
            path: { saksnummer },
            body: { aarsak },
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <Dialog.Popup closeOnOutsideClick={false}>
                <Dialog.Header>
                    <Dialog.Title>Gjenåpne sak</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                    <VStack gap="space-32">
                        <BodyLong>
                            Saken er i dag lukket, og ved å gjenåpne saken vil den bli aktiv igjen og kunne behandles
                            videre. Det er viktig at du oppgir en årsak til hvorfor saken gjenåpnes, slik at det blir
                            dokumentert i saken.
                        </BodyLong>
                        <Textarea
                            label="Årsak"
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}
                        />
                        <ErrorAlert error={gjenapne.error} />
                    </VStack>
                </Dialog.Body>
                <Dialog.Footer>
                    <Button type="button" variant="primary" disabled={!hasPermission} onClick={onGjenopprett}>
                        Gjenåpne
                    </Button>
                    <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
                        Angre
                    </Button>
                </Dialog.Footer>
            </Dialog.Popup>
        </Dialog>
    );
}
