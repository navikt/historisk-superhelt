import {Button, ErrorSummary, HStack, InfoCard, TextField, VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {useState} from "react";
import {useMutation, useQueryClient, useSuspenseQuery} from "@tanstack/react-query";
import {getOrCreateBrevQueryKey} from "~/routes/sak/$saksnummer/-api/brev.query";
import {
    hentBrevOptions,
    hentBrevQueryKey,
    htmlBrevOptions,
    oppdaterBrevMutation
} from "@generated/@tanstack/react-query.gen";
import {useAutoSave} from "~/common/useAutosave";
import {Brev, Sak} from "@generated";

interface BrevEditorProps {
    sak: Sak,
    brevId?: string,
    readOnly?: boolean,
    onSuccess: (brevId: string) => Promise<void>,
    buttonText: string,
}

export function BrevEditor(props: BrevEditorProps) {
    if (!props.brevId) return <InfoCard data-color="warning">
        <InfoCard.Header>
            <InfoCard.Title>Finner ikke brev</InfoCard.Title>
        </InfoCard.Header>
        <InfoCard.Content>
            Det finnes ikke noe brev av denne typen i denne saken
        </InfoCard.Content>
    </InfoCard>

    return <BrevEditorInternal {...props} brevId={props.brevId}/>
}

interface BrevEditorInternalProps extends BrevEditorProps {
    brevId: string,
}

function BrevEditorInternal({sak, brevId, readOnly, onSuccess, buttonText}: BrevEditorInternalProps) {
    const saksnummer = sak.saksnummer
    const {data: brev} = useSuspenseQuery({
        ...hentBrevOptions({
            path: {
                saksnummer: saksnummer,
                brevId: brevId,
            }
        })
    })
    const queryClient = useQueryClient();
    const {data: genpdfHtml} = useSuspenseQuery({
        ...htmlBrevOptions({
            path: {
                saksnummer: saksnummer,
                brevId: brevId,
            },
        }),
        refetchOnWindowFocus: false,

    })
    const [editorContent, setEditorContent] = useState(brev?.innhold ?? "")
    const [tittel, setTittel] = useState(brev?.tittel ?? "")

    const [hasChanged, setHasChanged] = useState(false)

    const [showValidation, setShowValidation] = useState(false)
    const [loading, setLoading] = useState(false)
    const validationErrors = brev?.valideringsfeil || []
    const hasValidationErrors = validationErrors.length > 0

    const oppdaterBrev = useMutation({
        ...oppdaterBrevMutation(),
        onSuccess: (data) => {
            queryClient.setQueryData(getOrCreateBrevQueryKey(saksnummer, brev.type, brev.mottakerType), data)
            queryClient.invalidateQueries({
                queryKey: hentBrevQueryKey({
                    path: {
                        saksnummer: saksnummer,
                        brevId: brevId
                    }
                })
            })
        }
    })

    async function lagreBrev(): Promise<Brev | undefined> {
        if (readOnly) return undefined;
        if (!hasChanged) return brev;
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

    async function onActionClick() {
        setLoading(true)
        const lagretBrev = await lagreBrev();
        setShowValidation(true)
        if (lagretBrev && lagretBrev.valideringsfeil.length === 0) {
            await onSuccess(brevId)
        }
        setLoading(false)
    }

    const hasError: boolean = showValidation && (!!oppdaterBrev?.error || hasValidationErrors)

    return (
        <VStack gap={"space-32"}>
            <TextField label={"Dokumentbeskrivelse i arkivet"} value={tittel} readOnly={readOnly}
                       onChange={e => tittelChanged(e.target.value)}
                       error={getErrorMessage("tittel")}
                       onBlur={lagreBrev}/>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={editorChanged}
                              readOnly={readOnly}
                              error={getErrorMessage("innhold")}
                              onBlur={lagreBrev}
            />
            <HStack gap="space-32" align="start">
                <Button type="submit" variant="primary" onClick={onActionClick} disabled={readOnly}
                        loading={loading}>{buttonText}</Button>
            </HStack>
            {hasError && <ErrorSummary>
                {oppdaterBrev.error && <ErrorSummary.Item>{oppdaterBrev?.error?.detail}</ErrorSummary.Item>}
                {validationErrors.map((feil) => (
                    <ErrorSummary.Item key={feil.field}>{feil.message}</ErrorSummary.Item>
                ))}

            </ErrorSummary>}
        </VStack>
    );
}


