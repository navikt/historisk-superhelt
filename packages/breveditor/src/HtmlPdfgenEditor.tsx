import "./aksel-brev.css";
import { Box, ReadMore, VStack } from "@navikt/ds-react";
import { FixedHtml } from "./FixedHtml";
import { finnRedigerbartInnhold, toXhtml, utledPostfixInnhold, utledPrefiksInnhold } from "./pdfgen.utils";
import TiptapEditor from "./TiptapEditor";

interface HtmlPdfgenEditorProps {
    html: string;
    onChange: (html: string) => void;
    onBlur?: () => void;
    error?: string | undefined;
    readOnly?: boolean;
    saveStatus?: "idle" | "saving" | "saved" | "error";
    onSave?: () => Promise<void>;
    previewUrl?: string;
}

export function HtmlPdfgenEditor({
    html,
    onChange,
    onBlur,
    error,
    readOnly,
    saveStatus,
    onSave,
    previewUrl,
}: HtmlPdfgenEditorProps) {
    const xhtml = toXhtml(html);
    const prefix = utledPrefiksInnhold(xhtml);
    const postfix = utledPostfixInnhold(xhtml);
    const editorContent = finnRedigerbartInnhold(html);

    return (
        <Box
            className="htmleditor"
            background="neutral-soft"
            borderWidth="1"
            borderRadius="8"
            borderColor="neutral-subtle"
        >
            <VStack gap="space-16" className="brev-layout">
                <VStack gap="space-24" paddingBlock="space-16">
                    <FixedHtml html={prefix} />
                    <TiptapEditor
                        initialContentHtml={editorContent}
                        onChange={onChange}
                        error={error}
                        onBlur={onBlur}
                        readOnly={readOnly}
                        saveStatus={saveStatus}
                        onSave={onSave}
                        previewUrl={previewUrl}
                    />
                    <ReadMore header="Standardtekster og signatur" variant="moderate" data-color="neutral">
                        <FixedHtml html={postfix} />
                    </ReadMore>
                </VStack>
            </VStack>
        </Box>
    );
}
