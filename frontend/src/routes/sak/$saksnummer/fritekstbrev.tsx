import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {ErrorSummary, Modal, VStack} from "@navikt/ds-react";
import {useMutation, useQuery, useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useRef} from "react";
import {BrevEditor} from "~/routes/sak/$saksnummer/-components/BrevEditor";
import {DocPencilIcon} from "@navikt/aksel-icons";
import {sendBrevMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";

export const Route = createFileRoute('/sak/$saksnummer/fritekstbrev')({
    component: FritekstBrevPage,
})

function FritekstBrevPage() {
    const {saksnummer} = Route.useParams()
    const {data: sak} = useSuspenseQuery(getSakOptions(saksnummer))
    const hasSaksbehandleRettighet = sak.rettigheter.includes("SAKSBEHANDLE")
    const {data: brev} = useQuery({
            ...getOrCreateBrevOptions(saksnummer, "FRITEKSTBREV", "BRUKER"),
            enabled: hasSaksbehandleRettighet
        }
    )
    const ref = useRef<HTMLDialogElement>(null);
    const navigate = useNavigate();
    const invalidateSakQuery = useInvalidateSakQuery();

    const sendBrev = useMutation({
        ...sendBrevMutation(),
        onSuccess: (data) => {
            invalidateSakQuery(saksnummer)
            navigateBack()
        }
    })

    const navigateBack = () => {
        navigate({to: "/sak/$saksnummer/oppsummering", params: {saksnummer}});
    }

    const onBrevSend = async (brevId: string) => {
        await sendBrev.mutateAsync({
            path: {
                saksnummer: saksnummer,
                brevId: brevId
            }
        })

    }
    const hasError = !!sendBrev.error

    return (
        <VStack gap={"space-32"}>
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
                    <BrevEditor sak={sak}
                                brevId={brev?.uuid}
                                buttonText="Send brev"
                                onSuccess={onBrevSend}
                                readOnly={!hasSaksbehandleRettighet}
                    />

                    {hasError && <ErrorSummary>
                        {sendBrev.error && <ErrorSummary.Item>{sendBrev?.error?.detail}</ErrorSummary.Item>}
                    </ErrorSummary>}
                </Modal.Body>

            </Modal>
        </VStack>
    );
}


