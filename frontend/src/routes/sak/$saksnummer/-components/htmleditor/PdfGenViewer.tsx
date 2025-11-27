import './HTMLEditor.css'
import {
    toXhtml,
    utledPostfixInnhold,
    utledPrefiksInnhold,
    utledStiler
} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.utils";
import {Box, VStack} from "@navikt/ds-react";


interface PDFGenViewerProps {
    html: string;
    children?: React.ReactNode;
}


interface HTMLEditorContentAndStyleProps {
    html: string;
    brevStiler: string;
}

function HTMLEditorContentAndStyle({html, brevStiler}: HTMLEditorContentAndStyleProps) {
    return (
        <div className="brevOgStilWrapper">
            {/** biome-ignore lint/security/noDangerouslySetInnerHtml: html er sjekket fra før */}
            <div dangerouslySetInnerHTML={{__html: html}}/>
        </div>
    )
}

export function PDFGenViewer({html, children}: PDFGenViewerProps) {
    const xhtml = toXhtml(html)
    const brevStiler = utledStiler(xhtml)
    const prefix = utledPrefiksInnhold(xhtml)
    const postfix = utledPostfixInnhold(xhtml)

    return <Box.New background={"neutral-moderate"} padding={"space-16"} >
    <VStack gap={"space-8"} >
        <HTMLEditorContentAndStyle html={prefix} brevStiler={brevStiler} />
        {children}
        <HTMLEditorContentAndStyle html={postfix} brevStiler={brevStiler} />
    </VStack>
    </Box.New>

}