import "./aksel-brev.css";
import { ReadMore, VStack } from "@navikt/ds-react";
import { FixedHtml } from "~/routes/sak/$saksnummer/-components/htmleditor/FixedHtml";
import {
    finnRedigerbartInnhold,
    toXhtml,
    utledPostfixInnhold,
    utledPrefiksInnhold,
} from "~/routes/sak/$saksnummer/-components/htmleditor/pdfgen.utils";
import TiptapEditor from "~/routes/sak/$saksnummer/-components/htmleditor/TiptapEditor";
import { Card } from "~/common/card/Card";

interface HtmlEditorProps {
    html: string;
    onChange: (html: string) => void;
    onBlur?: () => void;
    error?: string | undefined;
    readOnly?: boolean;
}

export function HtmlPdfgenEditor({ html, onChange, onBlur, error, readOnly }: HtmlEditorProps) {
    const xhtml = toXhtml(html);
    const prefix = utledPrefiksInnhold(xhtml);
    const postfix = utledPostfixInnhold(xhtml);
    const editorContent = finnRedigerbartInnhold(html);

    return (
        <Card className="htmleditor">
            <VStack gap="space-24" paddingBlock="space-16">
                <FixedHtml html={prefix} />
                <TiptapEditor
                    initialContentHtml={editorContent}
                    onChange={onChange}
                    error={error}
                    onBlur={onBlur}
                    readOnly={readOnly}
                />
                <ReadMore header="Standardtekster og signatur" variant="moderate" data-color="neutral">
                    <FixedHtml html={postfix} />
                </ReadMore>
            </VStack>
        </Card>
    );
}
