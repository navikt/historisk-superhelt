import {createFileRoute, useRouter} from '@tanstack/react-router'
import {Modal, VStack} from "@navikt/ds-react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef} from "react";
import {VedtaksBrevEditor} from "~/routes/sak/$saksnummer/-components/VedtaksBrevEditor";
import {DocPencilIcon} from "@navikt/aksel-icons";

export const Route = createFileRoute('/sak/$saksnummer/annetbrev')({
    component: OppsummeringPage,
})

function OppsummeringPage() {

    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const ref = useRef<HTMLDialogElement>(null);
    const router = useRouter();

    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE")

    const navigateBack = () => {
        router.history.back()
    }

    const onBrevSend = () => {
        navigateBack()
        alert("Brev sendt til bruker")
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
                                       onSucess={onBrevSend}
                                       readOnly={!hasSaksbehandleRettighet}
                    />
                </Modal.Body>

            </Modal>
        </VStack>)
}


