import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
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

    const oppdaterBrev = useMutation({
        ...oppdaterBrevMutation()
    })

    const lagreBrev = () => {
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


    const [editorContent, setEditorContent] = useState(brev?.innhold ?? "")
    useAutoSave(editorContent, lagreBrev, 2000 )


    const editorChanged = (html: string) => {
        // console.log("editor changed", html)
        setEditorContent(html)

    };

    return (
        <VStack gap={"8"}>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={editorChanged}/>
        </VStack>)
}


