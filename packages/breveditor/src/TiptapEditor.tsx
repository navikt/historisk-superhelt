import { Bleed, Box, ErrorMessage, HStack, InlineMessage, Loader } from "@navikt/ds-react";
import Highlight from "@tiptap/extension-highlight";
import { EditorContent, EditorContext, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { useEffect, useMemo } from "react";
import { MenuBar } from "./MenuBar";
import { PdfPreview } from "./PdfPreview";
import type { StandardtekstSearch } from "./Standardtekst.types";
import { StandardtekstExtension } from "./StandardtekstExtension";
import { StandardtekstModal } from "./StandardtekstModal";
import styles from "./TiptapEditor.module.css";
import { WordHighlight } from "./WordHighlight";

const extensions = [StarterKit, Highlight, WordHighlight, StandardtekstExtension];

interface TiptapEditorProps {
    initialContentHtml: string;
    onChange: (html: string) => void;
    onBlur?: () => void;
    error: string | undefined;
    readOnly?: boolean;
    saveStatus?: "idle" | "saving" | "saved" | "error";
    onSave?: () => Promise<void>;
    previewUrl?: string;
    searchStandardtekster: StandardtekstSearch;
}

function TiptapEditor({
    initialContentHtml,
    onChange,
    onBlur,
    error,
    readOnly,
    saveStatus,
    onSave,
    previewUrl,
    searchStandardtekster,
}: TiptapEditorProps) {
    const editor = useEditor({
        extensions,
        content: initialContentHtml,
        editable: !readOnly,
        onUpdate: (editorState) => {
            onChange(editorState.editor.getHTML());
        },
        onBlur: onBlur,
    });

    // Oppdater editor-innhold når initialContentHtml endres
    useEffect(() => {
        if (!editor) return;
        editor.chain().setContent(initialContentHtml, { emitUpdate: false }).run();
    }, [editor, initialContentHtml]);

    const providerValue = useMemo(() => ({ editor }), [editor]);

    async function handleManualSave() {
        await onSave?.();
    }

    async function saveBeforePreview() {
        onChange(editor.getHTML());
        await onSave?.();
    }
    return (
        <Box
            background="raised"
            borderRadius="8"
            borderWidth="1"
            borderColor="neutral-subtle"
            className={`brev-editor-ramme ${error ? styles.errorBorder : ""}`}
        >
            <Bleed marginBlock="space-1 space-0">
                {!readOnly && editor && (
                    <>
                        <MenuBar editor={editor} onSave={onSave ? handleManualSave : undefined}>
                            <Box marginInline="auto space-8">
                                <BrevStatus status={saveStatus} />
                            </Box>
                            <PdfPreview
                                previewUrl={previewUrl}
                                onBeforePreview={saveBeforePreview}
                                readOnly={readOnly}
                            />
                        </MenuBar>
                        <StandardtekstModal editor={editor} searchStandardtekster={searchStandardtekster} />
                    </>
                )}
            </Bleed>
            <EditorContext.Provider value={providerValue}>
                <EditorContent
                    editor={editor}
                    className={`brev-editor ${styles.editor}`}
                    data-testid="tiptapeditor"
                    onClick={() => editor?.commands.focus()}
                    onKeyDown={(event) => {
                        if (
                            event.key.toLowerCase() !== "s" ||
                            (!event.ctrlKey && !event.metaKey) ||
                            event.altKey ||
                            event.shiftKey ||
                            readOnly ||
                            !editor?.isEditable ||
                            !onSave
                        ) {
                            return;
                        }

                        event.preventDefault();
                        handleManualSave();
                    }}
                />
            </EditorContext.Provider>
            {error && <ErrorMessage showIcon>{error}</ErrorMessage>}
        </Box>
    );
}

function BrevStatus({ status }: { status: "idle" | "saving" | "saved" | "error" | undefined }) {
    switch (status) {
        case "idle":
            return null;
        case "saving":
            return (
                <HStack gap="space-12" align="center">
                    <Loader size="xsmall" />
                    Lagrer...
                </HStack>
            );
        case "saved":
            return (
                <InlineMessage size="small" status="success">
                    Lagret
                </InlineMessage>
            );
        case "error":
            return (
                <InlineMessage size="small" status="error">
                    Feil ved lagring
                </InlineMessage>
            );
        default:
            return null;
    }
}

export default TiptapEditor;
