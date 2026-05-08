import { Bleed, Box, Detail, ErrorMessage, HStack, InlineMessage, Loader, VStack } from "@navikt/ds-react";
import Highlight from "@tiptap/extension-highlight";
import StarterKit from "@tiptap/starter-kit";
import { EditorContent, EditorContext, useEditor } from "@tiptap/react";
import { useEffect, useMemo } from "react";
import styles from "./TiptapEditor.module.css";
import { MenuBar } from "./MenuBar";
import { WordHighlight } from "./WordHighlight";

const extensions = [StarterKit, Highlight, WordHighlight];
interface TiptapEditorProps {
    initialContentHtml: string;
    onChange: (html: string) => void;
    onBlur?: () => void;
    error: string | undefined;
    readOnly?: boolean;
    saveStatus?: "idle" | "saving" | "saved" | "error";
}

function TiptapEditor({ initialContentHtml, onChange, onBlur, error, readOnly, saveStatus }: TiptapEditorProps) {
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
        if (editor && initialContentHtml !== editor.getHTML()) {
            editor.commands.setContent(initialContentHtml, { emitUpdate: false });
        }
    }, [editor, initialContentHtml]);

    const providerValue = useMemo(() => ({ editor }), [editor]);

    return (
        <Box
            background="raised"
            paddingInline="space-24"
            borderRadius="8"
            borderWidth="1"
            borderColor="neutral-subtle"
            className={error ? styles.errorBorder : ""}
        >
            <Bleed marginInline="space-28" marginBlock="space-1 space-0">
                {!readOnly && editor && (
                    <MenuBar editor={editor}>
                        <Box marginInline="auto space-8">
                            <BrevStatus status={saveStatus} />
                        </Box>
                    </MenuBar>
                )}
            </Bleed>
            <EditorContext.Provider value={providerValue}>
                <EditorContent
                    editor={editor}
                    className={styles.editor}
                    data-testid="tiptapeditor"
                    onClick={() => editor?.commands.focus()}
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
                    Lagret kl.{" "}
                    {new Date().toLocaleTimeString("no-NO", {
                        hour: "2-digit",
                        minute: "2-digit",
                        second: "2-digit",
                    })}
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
