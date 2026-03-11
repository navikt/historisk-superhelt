import { tilbakestillGjenapningMutation } from "@generated/@tanstack/react-query.gen";
import { ArrowUndoIcon } from "@navikt/aksel-icons";
import { BodyLong, Button, Modal, Textarea, VStack } from "@navikt/ds-react";
import { useMutation, useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useRef, useState } from "react";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

export const Route = createFileRoute("/sak/$saksnummer/tilbakestill")({
    component: TilbakestillPage,
});

function TilbakestillPage() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const ref = useRef<HTMLDialogElement>(null);
    const navigate = useNavigate();
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("");
    const [error, setError] = useState<string | undefined>();

    const tilbakestill = useMutation({
        ...tilbakestillGjenapningMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
            navigateBack();
        },
    });

    const hasPermission = sak.rettigheter.includes("TILBAKESTILL_GJENAPNING");

    const navigateBack = () => {
        navigate({ to: "/sak/$saksnummer/opplysninger", params: { saksnummer } });
    };

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

    const onTilbakestill = async () => {
        if (!validate()) {
            return;
        }
        await tilbakestill.mutateAsync({
            path: {
                saksnummer: saksnummer,
            },
            body: {
                aarsak: aarsak,
            },
        });
    };

    return (
        <VStack gap={"space-32"}>
            <Modal
                ref={ref}
                open={true}
                onClose={navigateBack}
                header={{
                    icon: <ArrowUndoIcon aria-hidden />,
                    heading: "Tilbakestill sak",
                }}
            >
                <Modal.Body>
                    <VStack gap={"space-32"}>
                        <BodyLong>
                            Saken ble gjenåpnet ved en feil. Ved å tilbakestille saken vil den bli gjenopprettet til
                            tilstanden den hadde ved siste ferdigstilling. Det er viktig at du oppgir en årsak til
                            hvorfor saken tilbakestilles, slik at det blir dokumentert i saken.
                        </BodyLong>
                        <Textarea
                            label={"Årsak"}
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}
                        />
                        <ErrorAlert error={tilbakestill.error} />
                    </VStack>
                </Modal.Body>
                <Modal.Footer>
                    <Button type="button" variant="primary" disabled={!hasPermission} onClick={onTilbakestill}>
                        Tilbakestill
                    </Button>
                    <Button type="button" variant="secondary" onClick={navigateBack}>
                        Angre
                    </Button>
                </Modal.Footer>
            </Modal>
        </VStack>
    );
}
