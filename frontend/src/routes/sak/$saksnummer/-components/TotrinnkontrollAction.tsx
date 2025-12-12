import {BodyLong, Button, ErrorSummary, Heading, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import {useMutation} from "@tanstack/react-query";
import {sendTilAttesteringMutation} from "@generated/@tanstack/react-query.gen";
import {useInvalidateSakQuery} from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

interface Props {
    sak: Sak
}

export default function TotrinnkontrollAction({sak}: Props) {
    const invalidateSakQuery = useInvalidateSakQuery();
    const saksnummer = sak.saksnummer

    const sendTilTotrinn = useMutation({
        ...sendTilAttesteringMutation()
        , onSettled: () => {
            invalidateSakQuery(saksnummer)
        }
    })

    async function onClick() {
        sendTilTotrinn.mutate({
                path: {
                    saksnummer: saksnummer
                }
            }
        )
    }

    const hasRettighet = sak.rettigheter.includes("SAKSBEHANDLE")
    const valideringsfeil = sak.valideringsfeil.concat(sak.vedtaksbrevBruker?.valideringsfeil ?? [])
    const hasValideringsfeil = valideringsfeil.length > 0
    const hasError = hasValideringsfeil || !!sendTilTotrinn.error


    return <VStack gap={"space-16"}>
        <Heading size="medium">Til attestering</Heading>
        <BodyLong>En annen saksbehandler må verifisere vedtaket for at det skal bli gyldig</BodyLong>
        <Button
            variant="primary"
            disabled={!hasRettighet || hasValideringsfeil}
            onClick={onClick}
            loading={sendTilTotrinn?.status === 'pending'}
        >
            Send til attestering
        </Button>
        {hasError && <ErrorSummary>
            {sendTilTotrinn.error && <ErrorSummary.Item>{sendTilTotrinn?.error?.detail}</ErrorSummary.Item>}
            {valideringsfeil.map((feil) => (
                <ErrorSummary.Item key={feil.field}>{feil.message}</ErrorSummary.Item>
            ))}

        </ErrorSummary>}
    </VStack>

}