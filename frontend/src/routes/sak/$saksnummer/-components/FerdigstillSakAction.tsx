import {BodyLong, Button, ErrorSummary, Heading, Radio, RadioGroup, Textarea, VStack} from "@navikt/ds-react";
import {Sak} from "@generated";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {attersterSakMutation} from "@generated/@tanstack/react-query.gen";
import {sakQueryKey} from "~/routes/sak/$saksnummer/-api/sak.query";
import {useState} from "react";

interface Props {
    sak: Sak
}

type RadioValue = "godkjent" | "ikke_godkjent"| "init"

interface ValideringState {
    beslutning?: string
    kommentar?: string
}

export default function FerdigstillSakAction({sak}: Props) {
    const queryClient = useQueryClient();
    const saksnummer = sak.saksnummer
    const [beslutning, setBeslutning] = useState<RadioValue >("init");
    const [kommentar, setKommentar] = useState("")
    const [validering, setValidering] = useState<ValideringState>({})

    const attesterSak = useMutation({
        ...attersterSakMutation()
        , onSettled: () => {
            queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    function validate() {
        const beslutningValid = beslutning !== "init"
        const kommentarValid = beslutning === "godkjent" || (beslutning === "ikke_godkjent" && kommentar.trim().length > 5)
        setValidering({
            beslutning: beslutningValid ? undefined : "Du velge ett alternativ",
            kommentar: kommentarValid ? undefined : "Du må oppgi en årsak på minst 5 tegn ved avslag"
        })
        return beslutningValid && kommentarValid
    }
    function clearValidation() {
        setValidering({})
    }

    async function fatteVedtak() {
        const isValid = validate()
        if (!isValid) return;

        attesterSak.mutate({
                path: {
                    saksnummer: saksnummer
                },
                body: {
                    godkjent: beslutning === "godkjent",
                    kommentar: kommentar
                }
            }
        )
    }

    const changeBeslutning = (value: RadioValue) => {
        clearValidation()
        setBeslutning(value)
    }
    const changeKommentar = (value: string) => {
        clearValidation()
        setKommentar(value)
    }

    const hasRettighet = sak.rettigheter.includes("ATTESTERE")
    const hasError =  !!attesterSak.error

    return <VStack gap={"space-16"}>
        <Heading size="medium">Godkjenne sak</Heading>
        <BodyLong>Saken må attesteres for å bli fullført </BodyLong>
        <RadioGroup legend="Velgresultat"
                    onChange={changeBeslutning}
                    value={beslutning} disabled={!hasRettighet}
                    error={validering.beslutning}>
            <Radio value="godkjent">Godkjenn vedtak</Radio>
            <Radio value="ikke_godkjent">Underkjenn og send tilbake til saksbehandler</Radio>
        </RadioGroup>
        {beslutning === "ikke_godkjent" && <Textarea
            label={"Årsak til avslag"}
            value={kommentar}
            error={validering.kommentar}
            onChange={(e) => changeKommentar(e.target.value)}
            readOnly={!hasRettighet}
        >
        </Textarea>
        }
        <Button
            variant="primary"
            disabled={!hasRettighet}
            onClick={fatteVedtak}
            loading={attesterSak?.status === 'pending'}
        >
            Fatte vedtak
        </Button>
        {hasError && <ErrorSummary>
            {attesterSak.error && <ErrorSummary.Item>{attesterSak?.error?.detail}</ErrorSummary.Item>}


        </ErrorSummary>}
    </VStack>
}