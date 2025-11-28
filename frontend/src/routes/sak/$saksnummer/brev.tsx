import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
import TiptapEditor from "~/routes/sak/$saksnummer/-components/htmleditor/TiptapEditor";
import {HtmlEditor} from "~/routes/sak/$saksnummer/-components/htmleditor/HtmlEditor";
import {finnRedigerbartInnhold} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.utils";
import {html as htmlExample} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.html";

export const Route = createFileRoute('/sak/$saksnummer/brev')({
    component: BrevPage,
})



function BrevPage() {

    const html= htmlExample;
    const editorContent = finnRedigerbartInnhold(html)


    return (
        <VStack gap={"8"}>
            <HtmlEditor html={html}>
                <TiptapEditor initialContentHtml={editorContent} onChange={console.debug}/>
            </HtmlEditor>
        </VStack>)
}


