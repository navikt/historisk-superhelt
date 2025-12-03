import {Button, ErrorSummary, HStack, TextField, VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {useState} from "react";
import {useMutation, useQueryClient, useSuspenseQuery} from "@tanstack/react-query";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";
import {htmlBrevOptions, oppdaterBrevMutation} from "@generated/@tanstack/react-query.gen";
import {useAutoSave} from "~/components/useAutosave";
import {sakQueryKey} from "~/routes/sak/$saksnummer/-api/sak.query";
import {Sak} from "@generated";
import {useNavigate} from "@tanstack/react-router";
import {BrevMottakerType, BrevType} from "~/routes/sak/$saksnummer/-types/brev.types";


interface BrevEditorProps {
    sak: Sak,
    type: BrevType,
    mottaker: BrevMottakerType,
    readOnly?: boolean,

}

/** Editor for vedtaksbrev til bruker
 *
 * TODO sørge for at validering fungerer for alle typer brev
 */
export function VedtaksBrevEditor({sak, type, mottaker, readOnly}: BrevEditorProps) {
    const saksnummer = sak.saksnummer
    const {data: brev} = useSuspenseQuery(getOrCreateBrevOptions(saksnummer, type, mottaker))
    const navigate = useNavigate()
    const queryClient = useQueryClient();
    const brevId = brev?.uuid ?? "";
    const {data: genpdfHtml} = useSuspenseQuery({
        ...htmlBrevOptions({
            path: {
                saksnummer: saksnummer,
                brevId: brevId,
            }
        }),
        refetchOnWindowFocus: false
    })
    const [editorContent, setEditorContent] = useState(brev?.innhold ?? "")
    const [tittel, setTittel] = useState(brev?.tittel ?? "")

    const [hasChanged, setHasChanged] = useState(false)

    const [showValidation, setShowValidation] = useState(false)
    const validationErrors = sak.tilstand.vedtaksbrevBruker.valideringsfeil || []
    const hasValidationErrors = validationErrors.length > 0


    const oppdaterBrev = useMutation({
        ...oppdaterBrevMutation(),
        onSuccess: () => {
            return queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    const lagreBrev = () => {
        if (readOnly) return;
        if (!hasChanged) return;
        setHasChanged(false)
        return oppdaterBrev.mutateAsync({
            path: {
                saksnummer: saksnummer,
                brevId: brevId,
            },
            body: {
                innhold: editorContent,
                tittel: tittel,
            }
        })
    }

    useAutoSave(editorContent, lagreBrev, 2000)

    const editorChanged = (html: string) => {
        setHasChanged(true)
        setEditorContent(html)
    };

    const tittelChanged = (tekst: string) => {
        setHasChanged(true)
        setTittel(tekst)
    }

    function getErrorMessage(field: "vedtaksbrevBruker.tittel" | "vedtaksbrevBruker.innhold"): string | undefined {
        if (!showValidation || !hasValidationErrors) {
            return undefined
        }
        return validationErrors.find(feil => feil.field === field)?.message || undefined

    }

    async function completedBrev() {
        await lagreBrev();
        setShowValidation(true)
        if (!hasValidationErrors) {
            navigate({to: "/sak/$saksnummer/vedtak", params: {saksnummer}})
        }

    }

    const hasError: boolean = showValidation && (!!oppdaterBrev?.error || hasValidationErrors)

    return (
        <VStack gap={"8"}>
            <TextField label={"Dokumentbeskrivelse i arkivet"} value={tittel} readOnly={readOnly}
                       onChange={e => tittelChanged(e.target.value)}
                       error={getErrorMessage("vedtaksbrevBruker.tittel")}
                       onBlur={lagreBrev}/>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={editorChanged}
                              readOnly={readOnly}
                              error={getErrorMessage("vedtaksbrevBruker.innhold")}/>
            <HStack gap="8" align="start">
                <Button type="submit" variant="secondary" onClick={completedBrev} disabled={readOnly}>Lagre og gå
                    videre</Button>
            </HStack>
            {hasError && <ErrorSummary>
                {oppdaterBrev.error && <ErrorSummary.Item>{oppdaterBrev?.error?.detail}</ErrorSummary.Item>}
                {validationErrors.map((feil) => (
                    <ErrorSummary.Item key={feil.field}>{feil.message}</ErrorSummary.Item>
                ))}

            </ErrorSummary>}
        </VStack>)
}


