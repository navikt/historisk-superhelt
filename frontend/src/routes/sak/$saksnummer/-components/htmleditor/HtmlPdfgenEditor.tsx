import './aksel-brev.css'
import {
    finnRedigerbartInnhold,
    toXhtml,
    utledPostfixInnhold,
    utledPrefiksInnhold
} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.utils";
import {Box, VStack} from "@navikt/ds-react";
import {FixedHtml} from "~/routes/sak/$saksnummer/-components/htmleditor/FixedHtml";
import TiptapEditor from "~/routes/sak/$saksnummer/-components/htmleditor/TiptapEditor";
import {BrevExpandable} from "./BrevExpandable";


interface HtmlEditorProps {
    html: string;
    onChange: (html: string) => void;
}


export function HtmlPdfgenEditor({html, onChange}: HtmlEditorProps) {
    const xhtml = toXhtml(html)
    const prefix = utledPrefiksInnhold(xhtml)
    const postfix = utledPostfixInnhold(xhtml)
    const editorContent = finnRedigerbartInnhold(html)

    return <Box.New background={"neutral-moderate"} padding={"space-16"} className="htmleditor">
        <VStack gap={"space-8"}>
            <FixedHtml html={prefix}/>
            <TiptapEditor initialContentHtml={editorContent} onChange={onChange}/>
            <BrevExpandable title={"Standardtekster og signatur"}>
                <FixedHtml html={postfix}/>
            </BrevExpandable>
        </VStack>
    </Box.New>

}