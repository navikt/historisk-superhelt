import styles from './TiptapEditor.module.css'
import type {Editor} from '@tiptap/react'
import {EditorContent, EditorContext, useEditor, useEditorState} from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import React, {useEffect, useMemo} from 'react'
import {Box, ErrorMessage} from "@navikt/ds-react";
import {ArrowRedoIcon, ArrowUndoIcon, BulletListIcon, NumberListIcon} from "@navikt/aksel-icons";
import {Bold, Italic} from "~/routes/sak/$saksnummer/-components/htmleditor/Icons";

const extensions = [StarterKit]

function MenuBar({editor}: { editor: Editor }) {
    // Read the current editor's state, and re-render the component when it changes
    const editorState = useEditorState({
        editor,
        selector: ctx => {
            return {
                isBold: ctx.editor.isActive('bold') ?? false,
                canBold: ctx.editor.can().chain().toggleBold().run() ?? false,
                isItalic: ctx.editor.isActive('italic') ?? false,
                canItalic: ctx.editor.can().chain().toggleItalic().run() ?? false,
                isStrike: ctx.editor.isActive('strike') ?? false,
                canStrike: ctx.editor.can().chain().toggleStrike().run() ?? false,
                isCode: ctx.editor.isActive('code') ?? false,
                canCode: ctx.editor.can().chain().toggleCode().run() ?? false,
                canClearMarks: ctx.editor.can().chain().unsetAllMarks().run() ?? false,
                isParagraph: ctx.editor.isActive('paragraph') ?? false,
                isHeading1: ctx.editor.isActive('heading', {level: 1}) ?? false,
                isHeading2: ctx.editor.isActive('heading', {level: 2}) ?? false,
                isHeading3: ctx.editor.isActive('heading', {level: 3}) ?? false,
                isHeading4: ctx.editor.isActive('heading', {level: 4}) ?? false,
                isHeading5: ctx.editor.isActive('heading', {level: 5}) ?? false,
                isHeading6: ctx.editor.isActive('heading', {level: 6}) ?? false,
                isBulletList: ctx.editor.isActive('bulletList') ?? false,
                isOrderedList: ctx.editor.isActive('orderedList') ?? false,
                isCodeBlock: ctx.editor.isActive('codeBlock') ?? false,
                isBlockquote: ctx.editor.isActive('blockquote') ?? false,
                canUndo: ctx.editor.can().chain().undo().run() ?? false,
                canRedo: ctx.editor.can().chain().redo().run() ?? false,
            }
        },
    })

    const activeStyle = "is-active";
    return (
        <div className={styles.redigeringsMeny}>
            <button
                aria-label="Angre"
                onClick={() => editor.chain().focus().undo().run()}
                disabled={!editor.can().undo()}
            >
                <ArrowUndoIcon title="Angre"/>
            </button>
            <button
                aria-label="Gjør om igjen"
                onClick={() => editor.chain().focus().redo().run()}
                disabled={!editor.can().redo()}
            >
                <ArrowRedoIcon title="Gjør om igjen"/>
            </button>

            <button
                aria-label="Fet"
                onClick={() => editor.chain().focus().toggleBold().run()}
                className={editorState.isBold ? activeStyle : ''}
                disabled={!editorState.canBold}
            >
                <Bold/>
            </button>

            <button
                aria-label="Kursiv"
                onClick={() => editor.chain().focus().toggleItalic().run()}
                className={editorState.isItalic ? activeStyle : ''}
                disabled={!editorState.canItalic}
            >
                <Italic/>
            </button>
            <button
                aria-label="Heading 1"
                onClick={() => editor.chain().focus().toggleHeading({level: 1}).run()}
                className={editorState.isHeading1 ? activeStyle : ''}
            >
                H1
            </button>
            <button
                aria-label="Heading 2"
                onClick={() => editor.chain().focus().toggleHeading({level: 2}).run()}
                className={editorState.isHeading2 ? activeStyle : ''}
            >
                H2
            </button>
            <button
                aria-label="Heading 3"
                onClick={() => editor.chain().focus().toggleHeading({level: 3}).run()}
                className={editorState.isHeading3 ? activeStyle : ''}
            >
                H3
            </button>
            <button
                aria-label="Heading 4"
                onClick={() => editor.chain().focus().toggleHeading({level: 4}).run()}
                className={editorState.isHeading4 ? activeStyle : ''}
            >
                H4
            </button>
            <button
                aria-label="Punktliste"
                onClick={() => editor.chain().focus().toggleBulletList().run()}
                className={editorState.isBulletList ? activeStyle : ''}
            >
                <BulletListIcon title="Punkt liste"/>
            </button>
            <button
                aria-label="Nummerert liste"
                onClick={() => editor.chain().focus().toggleOrderedList().run()}
                className={editorState.isOrderedList ? activeStyle : ''}
            >
                <NumberListIcon title="Ordnet liste"/>
            </button>

        </div>
    )

}

interface TiptapEditorProps {
    initialContentHtml: string,
    onChange: (html: string) => void,
    onBlur?: () => void,
    error: string | undefined
    readOnly?: boolean
}

function TiptapEditor({initialContentHtml, onChange, onBlur, error, readOnly}: TiptapEditorProps) {
    const editor = useEditor({
        extensions,
        content: initialContentHtml,
        editable: !readOnly,
        onUpdate: (editorState) => {
            onChange(editorState.editor.getHTML())
        },
        onBlur: onBlur
    })


    // Oppdater editor-innhold når initialContentHtml endres
    useEffect(() => {
        if (editor && initialContentHtml !== editor.getHTML()) {
            editor.commands.setContent(initialContentHtml)
        }
    }, [editor, initialContentHtml])

    const providerValue = useMemo(() => ({editor}), [editor])


    return (
        <Box.New background={"raised"} padding={"space-4"} className={error ? styles.errorBorder : ''}>
            <EditorContext.Provider value={providerValue}>
                {!readOnly && <MenuBar editor={editor}/>}
                <EditorContent editor={editor} className={styles.editor}/>
            </EditorContext.Provider>
            {error && <ErrorMessage showIcon>{error}</ErrorMessage>}
        </Box.New>
    )
}

export default TiptapEditor
