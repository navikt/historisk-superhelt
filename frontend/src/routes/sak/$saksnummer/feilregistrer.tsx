import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {BodyLong, Button, Modal, Textarea, VStack} from "@navikt/ds-react";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef, useState} from "react";
import {TrashIcon} from "@navikt/aksel-icons";
import {feilregisterSakMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import {ErrorAlert} from "~/common/error/ErrorAlert";

export const Route = createFileRoute('/sak/$saksnummer/feilregistrer')({
    component: FeilregistrerPage,
})

function FeilregistrerPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const ref = useRef<HTMLDialogElement>(null);
    const navigate = useNavigate();
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("")
    const [error, setError] = useState<string | undefined>()

    const feilregister = useMutation({
        ...feilregisterSakMutation(),
        onSuccess: (data) => {
            invalidateSakQuery(saksnummer)
            navigateBack()
        }
    })

    const hasPermission = sak.rettigheter.includes("FEILREGISTERE")

    const navigateBack = () => {
        navigate({to: "/sak/$saksnummer/oppsummering", params: {saksnummer}});
    }

    const validate = () => {
        if (aarsak.length < 5) {
            setError("Årsak må være minst 10 tegn")
            return false
        }
        if (aarsak.length > 1000) {
            setError("Årsak kan max være 1000 tegn")
            return false
        }
        setError(undefined)
        return true
    }

    const onFeilregistrer = async () => {
        if (!validate()) {
            return
        }
        await feilregister.mutateAsync({
            path: {
                saksnummer: saksnummer
            },
            body: {
                aarsak: aarsak
            }
        })
    }

    return (
        <VStack gap={"space-32"}>
            <Modal ref={ref}
                   open={true}
                   onClose={navigateBack}
                   header={{
                       icon: <TrashIcon aria-hidden/>,
                       heading: "Feilregistrer sak",
                   }}
            >
                <Modal.Body>
                    <VStack gap={"space-32"}>
                        <BodyLong>Saken feilregisteres og lukkes. Det er ikke mulig å åpne saken igjen etterpå. Det
                            blir laget en oppgave i Gosys for saksbehandler om å rydde opp i saken.
                        </BodyLong>

                        <Textarea
                            label={"Årsak"}
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}/>
                        <ErrorAlert error={feilregister.error}/>

                    </VStack>

                </Modal.Body>
                <Modal.Footer>
                    <Button
                        data-color="danger"
                        type="button"
                        variant="primary"
                        disabled={!hasPermission}
                        onClick={onFeilregistrer}>Feilregister</Button>
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={navigateBack}>Angre</Button>

                </Modal.Footer>
            </Modal>
        </VStack>
    );
}


