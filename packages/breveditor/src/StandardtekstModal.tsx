import { ClipboardIcon } from "@navikt/aksel-icons";
import {
    Accordion,
    BodyShort,
    Button,
    Dialog,
    ErrorMessage,
    HStack,
    Label,
    Loader,
    Search,
    Tag,
    VStack,
} from "@navikt/ds-react";
import { TextSelection } from "@tiptap/pm/state";
import type { Editor } from "@tiptap/react";
import DOMPurify from "dompurify";
import { useRef, useState } from "react";
import type { Standardtekst, StandardtekstSearch } from "./Standardtekst.types";

export function StandardtekstModal({
    editor,
    searchStandardtekster,
}: {
    editor: Editor;
    searchStandardtekster: StandardtekstSearch;
}) {
    const [open, setOpen] = useState(false);
    const [query, setQuery] = useState("");
    const [standardtekster, setStandardtekster] = useState<Standardtekst[]>([]);
    const [selectedStandardtekst, setSelectedStandardtekst] = useState<Standardtekst | null>(null);
    const [loading, setLoading] = useState(false);
    const [hasError, setHasError] = useState(false);
    const savedSelection = useRef<{ from: number; to: number } | null>(null);
    const latestSearch = useRef(0);
    const isOpen = useRef(false);
    const focusEditorAfterClose = useRef(false);

    function closeModal() {
        isOpen.current = false;
        latestSearch.current += 1;
        setLoading(false);
        setOpen(false);
    }

    async function search(query: string) {
        const searchNumber = ++latestSearch.current;
        setLoading(true);
        setStandardtekster([]);
        setHasError(false);
        setSelectedStandardtekst(null);

        try {
            const results = await searchStandardtekster({ query });
            if (isOpen.current && searchNumber === latestSearch.current) {
                setStandardtekster(results);
            }
        } catch {
            if (isOpen.current && searchNumber === latestSearch.current) {
                setStandardtekster([]);
                setHasError(true);
            }
        } finally {
            if (isOpen.current && searchNumber === latestSearch.current) {
                setLoading(false);
            }
        }
    }

    return (
        <>
            <Button
                type="button"
                variant="tertiary"
                data-color="neutral"
                size="small"
                onClick={() => {
                    savedSelection.current = {
                        from: editor.state.selection.from,
                        to: editor.state.selection.to,
                    };
                    focusEditorAfterClose.current = false;
                    setQuery("");
                    setSelectedStandardtekst(null);
                    isOpen.current = true;
                    setOpen(true);
                    void search("");
                }}
                icon={<ClipboardIcon title="a11y-title" fontSize="1.5rem" />}
            >
                Standardtekster
            </Button>
            <Dialog
                open={open}
                onOpenChange={(nextOpen) => {
                    if (nextOpen) {
                        isOpen.current = true;
                        setOpen(true);
                    } else {
                        closeModal();
                    }
                }}
            >
                <Dialog.Popup
                    width="large"
                    returnFocusTo={() => (focusEditorAfterClose.current ? editor.view.dom : undefined)}
                >
                    <Dialog.Header>
                        <Dialog.Title>Sett inn standardtekst</Dialog.Title>
                    </Dialog.Header>
                    <Dialog.Body>
                        <VStack gap="space-16">
                            <Search
                                label="Søk etter standardtekst"
                                variant="simple"
                                value={query}
                                onChange={(nextQuery) => {
                                    setQuery(nextQuery);
                                    void search(nextQuery);
                                }}
                            />
                            {loading && (
                                <HStack gap="space-8" align="center" role="status">
                                    <Loader size="small" />
                                    Søker etter standardtekster …
                                </HStack>
                            )}
                            {hasError && (
                                <VStack gap="space-8">
                                    <ErrorMessage showIcon>Standardtekstene kunne ikke lastes inn.</ErrorMessage>
                                    <Button type="button" variant="secondary" onClick={() => void search(query)}>
                                        Prøv igjen
                                    </Button>
                                </VStack>
                            )}
                            {!loading && !hasError && standardtekster.length === 0 && (
                                <BodyShort>Ingen standardtekster funnet.</BodyShort>
                            )}
                            {standardtekster.length > 0 && (
                                <Accordion>
                                    {standardtekster.map((standardtekst) => (
                                        <Accordion.Item
                                            key={standardtekst.id}
                                            open={selectedStandardtekst?.id === standardtekst.id}
                                            onOpenChange={(itemOpen) =>
                                                setSelectedStandardtekst(itemOpen ? standardtekst : null)
                                            }
                                        >
                                            <Accordion.Header>
                                                <HStack justify={"space-between"} gap={"space-8"}>
                                                    <Label>{standardtekst.title}</Label>
                                                    <HStack gap="space-8">
                                                        {standardtekst.tags.map((tag) => (
                                                            <Tag size="small" variant={"moderate"}>
                                                                {tag}
                                                            </Tag>
                                                        ))}
                                                    </HStack>
                                                </HStack>
                                            </Accordion.Header>
                                            <Accordion.Content>
                                                <div
                                                    dangerouslySetInnerHTML={{
                                                        __html: DOMPurify.sanitize(standardtekst.html),
                                                    }}
                                                />
                                            </Accordion.Content>
                                        </Accordion.Item>
                                    ))}
                                </Accordion>
                            )}
                        </VStack>
                    </Dialog.Body>
                    <Dialog.Footer>
                        <Dialog.CloseTrigger>
                            <Button type="button" variant="secondary">
                                Avbryt
                            </Button>
                        </Dialog.CloseTrigger>
                        <Button
                            type="button"
                            variant="primary"
                            disabled={!selectedStandardtekst}
                            onClick={() => {
                                if (!selectedStandardtekst) return;
                                const selection = savedSelection.current;
                                const chain = editor.chain().focus();
                                if (selection) chain.setTextSelection(selection);
                                chain
                                    .insertContent(DOMPurify.sanitize(selectedStandardtekst.html))
                                    .command(({ tr }) => {
                                        const insertedContentEnd = tr.selection.from;
                                        if (selection && selection.from < insertedContentEnd) {
                                            let insertedTextStart: number | null = null;
                                            let insertedTextEnd: number | null = null;

                                            tr.doc.nodesBetween(
                                                selection.from,
                                                insertedContentEnd,
                                                (node, position) => {
                                                    if (!node.isText) return;

                                                    const textStart = Math.max(position, selection.from);
                                                    const textEnd = Math.min(
                                                        position + node.nodeSize,
                                                        insertedContentEnd,
                                                    );
                                                    if (textStart < textEnd) {
                                                        insertedTextStart =
                                                            insertedTextStart === null
                                                                ? textStart
                                                                : Math.min(insertedTextStart, textStart);
                                                        insertedTextEnd =
                                                            insertedTextEnd === null
                                                                ? textEnd
                                                                : Math.max(insertedTextEnd, textEnd);
                                                    }
                                                },
                                            );

                                            if (insertedTextStart !== null && insertedTextEnd !== null) {
                                                tr.setSelection(
                                                    TextSelection.create(tr.doc, insertedTextStart, insertedTextEnd),
                                                );
                                            }
                                        }
                                        return true;
                                    })
                                    .run();
                                focusEditorAfterClose.current = true;
                                closeModal();
                            }}
                        >
                            Sett inn tekst
                        </Button>
                    </Dialog.Footer>
                </Dialog.Popup>
            </Dialog>
        </>
    );
}
