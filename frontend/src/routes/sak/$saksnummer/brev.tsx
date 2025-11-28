import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
import {HtmlPdfgenEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlPdfgenEditor";
import {html as htmlExample} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.html";
import {useState} from "react";

export const Route = createFileRoute('/sak/$saksnummer/brev')({
    component: BrevPage,
})



function BrevPage() {

    const html= htmlExample;
    const [editorContent, setEditorContent] = useState("")

    return (
        <VStack gap={"8"}>
            <HtmlPdfgenEditor html={html} onChange={setEditorContent} />
        </VStack>)
}


