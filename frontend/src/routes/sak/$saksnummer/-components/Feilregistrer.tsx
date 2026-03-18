import { feilregisterSakMutation } from "@generated/@tanstack/react-query.gen";
import { BodyLong, Button, Dialog, Textarea, VStack } from "@navikt/ds-react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import { useState } from "react";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface FeilregistrerProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function Feilregistrer({ open, onOpenChange }: FeilregistrerProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("");
    const [error, setError] = useState<string | undefined>();

    const feilregister = useMutation({
        ...feilregisterSakMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
            onOpenChange(false);
        },
    });

    const hasPermission = sak.rettigheter.includes("FEILREGISTERE");

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

    const onFeilregistrer = async () => {
        if (!validate()) return;
        await feilregister.mutateAsync({
            path: { saksnummer },
            body: { aarsak },
        });
    };

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <Dialog.Popup closeOnOutsideClick={false}>
                <Dialog.Header>
                    <Dialog.Title>Feilregistrer sak</Dialog.Title>
                </Dialog.Header>
                <Dialog.Body>
                    <VStack gap="space-32">
                        <BodyLong>
                            Saken feilregisteres og lukkes. Det er ikke mulig å åpne saken igjen etterpå. Det blir laget
                            en oppgave i Gosys for saksbehandler om å rydde opp i saken.
                        </BodyLong>
                        <Textarea
                            label="Årsak"
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}
                        />
                        <ErrorAlert error={feilregister.error} />
                    </VStack>
                </Dialog.Body>
                <Dialog.Footer>
                    <Button
                        data-color="danger"
                        type="button"
                        variant="primary"
                        disabled={!hasPermission}
                        onClick={onFeilregistrer}
                    >
                        Feilregister
                    </Button>
                    <Button type="button" variant="secondary" onClick={() => onOpenChange(false)}>
                        Angre
                    </Button>
                </Dialog.Footer>
            </Dialog.Popup>
        </Dialog>
    );
}
