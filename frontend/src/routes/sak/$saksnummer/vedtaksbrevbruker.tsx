import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Button, HStack, TextField, VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {useState} from "react";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";
import {htmlBrevOptions, oppdaterBrevMutation} from "@generated/@tanstack/react-query.gen";
import {useAutoSave} from "~/components/useAutosave";

export const Route = createFileRoute('/sak/$saksnummer/vedtaksbrevbruker')({
    component: BrevPage,
})


function BrevPage() {
    const {saksnummer} = Route.useParams()
    const {data: brev} = useSuspenseQuery(getOrCreateBrevOptions(saksnummer, "VEDTAKSBREV", "BRUKER"))
    const navigate = useNavigate()
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

    const oppdaterBrev = useMutation({
        ...oppdaterBrevMutation()
    })

    const lagreBrev = () => {
        if (!hasChanged) return;
        setHasChanged(false)
        oppdaterBrev.mutate({
            path: {
                saksnummer: saksnummer,
                brevId: brevId,
            },
            body: {
                innhold: editorContent,
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

    function completedBrev() {
        lagreBrev()
        navigate({to: "/sak/$saksnummer/vedtak", params: {saksnummer}})
    }

    return (
        <VStack gap={"8"}>
            <TextField label={"Dokumentbeskrivelse i arkivet"} value={tittel}
                       onChange={e => tittelChanged(e.target.value)}
                       onBlur={lagreBrev}/>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={editorChanged}/>
            <HStack gap="8" align="start">
                <Button type="submit" variant="secondary" onClick={completedBrev}>Lagre og gå videre</Button>
            </HStack>
        </VStack>)
}


