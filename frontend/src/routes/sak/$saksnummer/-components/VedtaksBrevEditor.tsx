import {Button, ErrorSummary, HStack, TextField, VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {useState} from "react";
import {useMutation, useQueryClient, useSuspenseQuery} from "@tanstack/react-query";
import {getOrCreateBrevOptions, getOrCreateBrevQueryKey} from "~/routes/sak/$saksnummer/-api/brev.query";
import {htmlBrevOptions, oppdaterBrevMutation} from "@generated/@tanstack/react-query.gen";
import {useAutoSave} from "~/components/useAutosave";
import {Sak} from "@generated";
import {BrevMottakerType, BrevType} from "~/routes/sak/$saksnummer/-types/brev.types";


interface BrevEditorProps {
    sak: Sak,
    type: BrevType,
    mottaker: BrevMottakerType,
    readOnly?: boolean,
    onSucess: () => void,
    buttonText: string,
}


/** Editor for vedtaksbrev til bruker
 */
export function VedtaksBrevEditor({sak, type, mottaker, readOnly, onSucess, buttonText}: BrevEditorProps) {
    const saksnummer = sak.saksnummer
    const {data: brev} = useSuspenseQuery(getOrCreateBrevOptions(saksnummer, type, mottaker))
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
    const validationErrors = brev?.valideringsfeil || []
    const hasValidationErrors = validationErrors.length > 0

    const oppdaterBrev = useMutation({
        ...oppdaterBrevMutation(),
        onSuccess: (data) => {
            queryClient.setQueryData(getOrCreateBrevQueryKey(saksnummer, type, mottaker), data)
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

    function getErrorMessage(field: "tittel" | "innhold"): string | undefined {
        if (!showValidation || !hasValidationErrors) {
            return undefined
        }
        return validationErrors.find(feil => feil.field === field)?.message || undefined

    }

    async function completedBrev() {
        await lagreBrev();
        setShowValidation(true)
        if (!hasValidationErrors) {
            onSucess()
        }

    }

    const hasError: boolean = showValidation && (!!oppdaterBrev?.error || hasValidationErrors)

    return (
        <VStack gap={"8"}>
            <TextField label={"Dokumentbeskrivelse i arkivet"} value={tittel} readOnly={readOnly}
                       onChange={e => tittelChanged(e.target.value)}
                       error={getErrorMessage("tittel")}
                       onBlur={lagreBrev}/>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={editorChanged}
                              readOnly={readOnly}
                              error={getErrorMessage("innhold")}/>
            <HStack gap="8" align="start">
                <Button type="submit" variant="secondary" onClick={completedBrev} disabled={readOnly}>{buttonText}</Button>
            </HStack>
            {hasError && <ErrorSummary>
                {oppdaterBrev.error && <ErrorSummary.Item>{oppdaterBrev?.error?.detail}</ErrorSummary.Item>}
                {validationErrors.map((feil) => (
                    <ErrorSummary.Item key={feil.field}>{feil.message}</ErrorSummary.Item>
                ))}

            </ErrorSummary>}
        </VStack>)
}


