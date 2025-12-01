import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {useState} from "react";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getOrCreateBrevOptions} from "~/routes/sak/$saksnummer/-api/brev.query";
import {htmlBrevOptions} from "@generated/@tanstack/react-query.gen";

export const Route = createFileRoute('/sak/$saksnummer/vedtaksbrevbruker')({
    component: BrevPage,
})




function BrevPage() {
    const {saksnummer} = Route.useParams()
    const {data: brev} = useSuspenseQuery(getOrCreateBrevOptions(saksnummer, "VEDTAKSBREV", "BRUKER"))
    const {data: genpdfHtml} = useSuspenseQuery(htmlBrevOptions({
        path: {
            saksnummer: saksnummer,
            brevId: brev?.uuid?? "",
        }
    }))


    const [editorContent, setEditorContent] = useState("")



    return (
        <VStack gap={"8"}>
            <HtmlPdfgenEditor html={genpdfHtml} onChange={setEditorContent} />
        </VStack>)
}


