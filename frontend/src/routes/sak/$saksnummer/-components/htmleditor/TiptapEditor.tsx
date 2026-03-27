import { Bleed, Box, ErrorMessage } from "@navikt/ds-react";
import Highlight from "@tiptap/extension-highlight";
import { EditorContent, EditorContext, useEditor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { useEffect, useMemo } from "react";
import { WordHighlight } from "./WordHighlight";
import styles from "./TiptapEditor.module.css";
import { MenuBar } from "./MenuBar";

const extensions = [StarterKit, Highlight, WordHighlight];
interface TiptapEditorProps {
    initialContentHtml: string;
    onChange: (html: string) => void;
    onBlur?: () => void;
    error: string | undefined;
    readOnly?: boolean;
}

function TiptapEditor({ initialContentHtml, onChange, onBlur, error, readOnly }: TiptapEditorProps) {
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
            padding="space-8"
            borderRadius="8"
            borderWidth="1"
            borderColor="neutral-subtle"
            className={error ? styles.errorBorder : ""}
            onClick={() => editor?.commands.focus()}
        >
            <Bleed marginInline="space-12" marginBlock="space-16 space-0">
                {!readOnly && <MenuBar editor={editor} />}
            </Bleed>
            <EditorContext.Provider value={providerValue}>
                <EditorContent editor={editor} className={styles.editor} />
            </EditorContext.Provider>
            {error && <ErrorMessage showIcon>{error}</ErrorMessage>}
        </Box>
    );
}

export default TiptapEditor;
