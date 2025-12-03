import {BodyLong, Button, ErrorSummary, Heading, Radio, RadioGroup, Textarea, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {ferdigstillSakMutation} from "@generated/@tanstack/react-query.gen";
import {sakQueryKey} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useState} from "react";

interface Props {
    sak: Sak
}

type RadioValue = "godkjent" | "ikke_godkjent"

export default function FerdigstillSakAction({sak}: Props) {
    const queryClient = useQueryClient();
    const saksnummer = sak.saksnummer
    const [val, setVal] = useState<RadioValue | undefined>();

    const ferdigStillSak = useMutation({
        ...ferdigstillSakMutation()
        , onSettled: () => {
            queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    async function fatteVedtak() {

        ferdigStillSak.mutate({
                path: {
                    saksnummer: saksnummer
                }
            }
        )
    }

    const hasRettighet = sak.rettigheter.includes("FERDIGSTILLE")
    const hasError = !hasRettighet || !!ferdigStillSak.error

    return <VStack gap={"space-16"}>
        <Heading size="medium">Godkjenne sak</Heading>
        <BodyLong>Saken må attesteres for å bli fullført </BodyLong>
        <RadioGroup legend="Velgresultat" onChange={setVal} value={val}>
            <Radio value="godkjent">Godkjenn vedtak</Radio>
            <Radio value="ikke_godkjent">Underkjenn og send tilbake til saksbehandler</Radio>
        </RadioGroup>
        {val === "ikke_godkjent" && <Textarea label={"Årsak til avslag"}>

        </Textarea>
        }
        <Button
            variant="primary"
            disabled={!hasRettighet}
            onClick={fatteVedtak}
            loading={ferdigStillSak?.status === 'pending'}
        >
            Fatte vedtak
        </Button>
        {hasError && <ErrorSummary>
            {!hasRettighet && <ErrorSummary.Item>Du må ha rettighet som attestant og kan ikke attestere din egen
                sak </ErrorSummary.Item>}
            {ferdigStillSak.error && <ErrorSummary.Item>{ferdigStillSak?.error?.detail}</ErrorSummary.Item>}


        </ErrorSummary>}
    </VStack>
}