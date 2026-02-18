import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Modal, Textarea, VStack} from "@navikt/ds-react";
import {useMutation, useQuery, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef, useState} from "react";
import {BrevEditor} from "~/routes/sak/$saksnummer/-components/BrevEditor";
import {henleggSakMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import {GavelIcon} from "@navikt/aksel-icons";
import {ErrorAlert} from "~/common/error/ErrorAlert";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";

export const Route = createFileRoute('/sak/$saksnummer/henlegg')({
    component: HenleggPage,
})

function HenleggPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const hasPermission = sak.rettigheter.includes("HENLEGGE")
    const {data: brev} = useQuery({
            ...getOrCreateBrevOptions(saksnummer, "HENLEGGESEBREV", "BRUKER"),
            enabled: hasPermission
        }
    )

    const ref = useRef<HTMLDialogElement>(null);
    const navigate = useNavigate();
    const invalidateSakQuery = useInvalidateSakQuery();
    const [aarsak, setAarsak] = useState("")
    const [error, setError] = useState<string | undefined>()

    const henleggMutation = useMutation({
        ...henleggSakMutation(),
        onSuccess: (data) => {
            invalidateSakQuery(saksnummer)
            navigateBack()
        }
    })

    const navigateBack = () => {
        navigate({to: "/sak/$saksnummer/oppsummering", params: {saksnummer}});
    }

    const validate = () => {
        if (aarsak.length < 10) {
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

    const onSubmit = async (brevId: string) => {
        if (!validate()) {
            return
        }

        await henleggMutation.mutateAsync({
            path: {
                saksnummer: saksnummer,
            },
            body: {
                henleggelseBrevId: brevId,
                aarsak: aarsak,
            }
        })

    }

    return (
        <VStack gap={"space-16"}>

            <Modal ref={ref}
                   open={true}
                   onClose={() => navigateBack()}
                   width={"80%"}
                   header={{
                       icon: <GavelIcon aria-hidden/>,
                       heading: "Henlegg sak",

                   }}
            >
                <Modal.Body>

                    <VStack gap={"space-16"}>
                        <Textarea
                            label={"Årsak til henleggelse"}
                            readOnly={!hasPermission}
                            value={aarsak}
                            onChange={(e) => setAarsak(e.target.value)}
                            error={error}/>


                        <BrevEditor
                            sak={sak}
                            brevId={brev?.uuid}
                            buttonText="Henlegg sak"
                            onSuccess={onSubmit}
                            readOnly={!hasPermission}
                        />

                        <ErrorAlert error={henleggMutation.error}/>
                    </VStack>
                </Modal.Body>

            </Modal>
        </VStack>
    )
}


