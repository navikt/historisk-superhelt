import type { SelectionBookmark } from "@tiptap/pm/state";
import { Plugin, PluginKey, TextSelection } from "@tiptap/pm/state";
import { Extension } from "@tiptap/react";
import DOMPurify from "dompurify";

interface StandardtekstModalState {
    isOpen: boolean;
    bookmark: SelectionBookmark | null;
}

type StandardtekstModalAction = { type: "open"; bookmark: SelectionBookmark } | { type: "close" };

export const standardtekstPluginKey = new PluginKey<StandardtekstModalState>("standardtekstModal");

declare module "@tiptap/react" {
    interface Commands<ReturnType> {
        standardtekst: {
            openStandardtekstModal: () => ReturnType;
            closeStandardtekstModal: () => ReturnType;
            insertStandardtekst: (html: string) => ReturnType;
        };
    }
}

export const StandardtekstExtension = Extension.create({
    name: "standardtekst",

    addProseMirrorPlugins() {
        return [
            new Plugin<StandardtekstModalState>({
                key: standardtekstPluginKey,
                state: {
                    init: () => ({ isOpen: false, bookmark: null }),
                    apply(transaction, previousState) {
                        const action = transaction.getMeta(standardtekstPluginKey) as
                            | StandardtekstModalAction
                            | undefined;
                        if (action?.type === "close") return { isOpen: false, bookmark: null };

                        if (action?.type === "open") {
                            return {
                                isOpen: true,
                                bookmark: action.bookmark.map(transaction.mapping),
                            };
                        }

                        if (!previousState.bookmark) return previousState;
                        return {
                            ...previousState,
                            bookmark: previousState.bookmark.map(transaction.mapping),
                        };
                    },
                },
            }),
        ];
    },

    addCommands() {
        return {
            openStandardtekstModal:
                () =>
                ({ editor, state, tr, dispatch }) => {
                    if (!editor.isEditable) return false;

                    const modalState = standardtekstPluginKey.getState(state);
                    if (modalState?.isOpen) return true;
                    if (dispatch) {
                        tr.setMeta(standardtekstPluginKey, {
                            type: "open",
                            bookmark: state.selection.getBookmark(),
                        } satisfies StandardtekstModalAction);
                    }
                    return true;
                },
            closeStandardtekstModal:
                () =>
                ({ tr, dispatch }) => {
                    if (dispatch)
                        tr.setMeta(standardtekstPluginKey, { type: "close" } satisfies StandardtekstModalAction);
                    return true;
                },
            insertStandardtekst:
                (html: string) =>
                ({ editor, state, tr, dispatch, chain }) => {
                    if (!editor.isEditable) return false;

                    const modalState = standardtekstPluginKey.getState(state);
                    if (!modalState?.isOpen || !modalState.bookmark) return false;
                    if (!dispatch) return true;

                    const selection = modalState.bookmark.map(tr.mapping).resolve(tr.doc);
                    return chain()
                        .command(({ tr }) => {
                            tr.setSelection(selection);
                            return true;
                        })
                        .insertContent(DOMPurify.sanitize(html))
                        .command(({ tr }) => {
                            const insertedContentEnd = tr.selection.from;
                            if (selection.from < insertedContentEnd) {
                                let insertedTextStart: number | null = null;
                                let insertedTextEnd: number | null = null;

                                tr.doc.nodesBetween(selection.from, insertedContentEnd, (node, position) => {
                                    if (!node.isText) return;

                                    const textStart = Math.max(position, selection.from);
                                    const textEnd = Math.min(position + node.nodeSize, insertedContentEnd);
                                    if (textStart < textEnd) {
                                        insertedTextStart =
                                            insertedTextStart === null
                                                ? textStart
                                                : Math.min(insertedTextStart, textStart);
                                        insertedTextEnd =
                                            insertedTextEnd === null ? textEnd : Math.max(insertedTextEnd, textEnd);
                                    }
                                });

                                if (insertedTextStart !== null && insertedTextEnd !== null) {
                                    tr.setSelection(TextSelection.create(tr.doc, insertedTextStart, insertedTextEnd));
                                }
                            }

                            return true;
                        })
                        .closeStandardtekstModal()
                        .run();
                },
        };
    },

    addKeyboardShortcuts() {
        return {
            "Ctrl-Shift-y": () => this.editor.commands.openStandardtekstModal(),
        };
    },
});
