import {createFileRoute, useRouter} from '@tanstack/react-router'
import {Modal, VStack} from "@navikt/ds-react";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef} from "react";
import {VedtaksBrevEditor} from "~/routes/sak/$saksnummer/-components/VedtaksBrevEditor";
import {DocPencilIcon} from "@navikt/aksel-icons";
import {sendBrevMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

export const Route = createFileRoute('/sak/$saksnummer/annetbrev')({
    component: AnnetBrevPage,
})

function AnnetBrevPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const ref = useRef<HTMLDialogElement>(null);
    const router = useRouter();
    const invalidateSakQuery = useInvalidateSakQuery();

    const sendBrev = useMutation({
        ...sendBrevMutation()
    })

    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE")

    const navigateBack = () => {
        router.history.back()
    }

    const onBrevSend = (brevId: string) => {
        sendBrev.mutate({
            path: {
                saksnummer: saksnummer,
                brevId: brevId
            }
        })
        invalidateSakQuery(saksnummer)
        navigateBack()
    }

    return (
        <VStack gap={"8"}>

            <Modal ref={ref}
                   open={true}
                   onClose={() => navigateBack()}
                   width={"80%"}
                   header={{
                       icon: <DocPencilIcon aria-hidden/>,
                       heading: "Skriv brev til bruker",
                   }}
            >
                <Modal.Body>
                    <VedtaksBrevEditor sak={sak} type={"INNHENTINGSBREV"} mottaker="BRUKER"
                                       buttonText="Send brev"
                                       onSuccess={onBrevSend}
                                       readOnly={!hasSaksbehandleRettighet}
                    />
                </Modal.Body>

            </Modal>
        </VStack>)
}


