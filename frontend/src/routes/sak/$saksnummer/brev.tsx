import {createFileRoute} from '@tanstack/react-router'
import {VStack} from "@navikt/ds-react";
import TiptapEditor from "~/routes/sak/$saksnummer/-components/htmleditor/TiptapEditor";
import {PDFGenViewer} from "~/routes/sak/$saksnummer/-components/htmleditor/PdfGenViewer";
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
            {/*<ExpansionCard aria-label={"header og til"}>*/}
            {/*    <ExpansionCard.Header>*/}
            {/*        <ExpansionCard.Title as="h4" size={"small"}>Mottaker osv</ExpansionCard.Title>*/}
            {/*    </ExpansionCard.Header>*/}
            {/*    <ExpansionCard.Content>*/}
            {/*        Her kommer tekst i topp*/}
            {/*    </ExpansionCard.Content>*/}
            {/*</ExpansionCard>*/}
            <PDFGenViewer html={html}>
                <TiptapEditor initialContentHtml={editorContent} onChange={console.debug}/>
            </PDFGenViewer>
            {/*<ExpansionCard aria-label={"Standardtekster og signatur"}>*/}
            {/*    <ExpansionCard.Header>*/}
            {/*        <ExpansionCard.Title as="h4" size={"small"}>Standardtekster og signatur</ExpansionCard.Title>*/}
            {/*    </ExpansionCard.Header>*/}
            {/*    <ExpansionCard.Content>*/}
            {/*      Her kommer tekst i bunn*/}
            {/*    </ExpansionCard.Content>*/}
            {/*</ExpansionCard>*/}

        </VStack>)
}


