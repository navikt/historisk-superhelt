import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {BodyLong, Button, Modal, Textarea, VStack} from "@navikt/ds-react";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef, useState} from "react";
import {PadlockUnlockedIcon} from "@navikt/aksel-icons";
import {gjenapneSakMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import {ErrorAlert} from "~/common/error/ErrorAlert";

export const Route = createFileRoute('/sak/$saksnummer/gjenapne')({
    component: GjenapnePage,
})

function GjenapnePage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const ref = useRef<HTMLDialogElement>(null);
    const navigate = useNavigate();
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("")
    const [error, setError] = useState<string | undefined>()

    const gjenapne = useMutation({
        ...gjenapneSakMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer)
            navigateBack()
        }
    })

    // TODO foreløpig skal ingen kunne gjenåpne saker, så denne er deaktivert. Når det blir aktuelt å åpne opp for dette
    // const hasPermission = sak.rettigheter.includes("GJENAPNE")
    const hasPermission = false

    const navigateBack = () => {
        navigate({to: "/sak/$saksnummer/opplysninger", params: {saksnummer}});
    }

    const validate = () => {
        if (aarsak.length < 5) {
            setError("Årsak må være minst 5 tegn")
            return false
        }
        if (aarsak.length > 1000) {
            setError("Årsak kan max være 1000 tegn")
            return false
        }
        setError(undefined)
        return true
    }

    const onGjenopprett = async () => {
        if (!validate()) {
            return
        }
        await gjenapne.mutateAsync({
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
                       icon: <PadlockUnlockedIcon aria-hidden/>,
                       heading: "Gjenåpne sak",
                   }}
            >
                <Modal.Body>
                    <VStack gap={"space-32"}>
                        <BodyLong>Saken er i dag lukket, og ved å gjenåpne saken vil den bli aktiv igjen og kunne behandles videre.
                            Det er viktig at du oppgir en årsak til hvorfor saken gjenåpnes, slik at det blir dokumentert i saken.
                        </BodyLong>

                        <Textarea
                            label={"Årsak"}
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}/>
                        <ErrorAlert error={gjenapne.error}/>

                    </VStack>

                </Modal.Body>
                <Modal.Footer>
                    <Button
                        type="button"
                        variant="primary"
                        disabled={!hasPermission}
                        onClick={onGjenopprett}>Gjenåpne</Button>
                    <Button
                        type="button"
                        variant="secondary"
                        onClick={navigateBack}>Angre</Button>

                </Modal.Footer>
            </Modal>
        </VStack>
    );
}


