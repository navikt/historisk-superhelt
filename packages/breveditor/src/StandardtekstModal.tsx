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
import { type Editor, useEditorState } from "@tiptap/react";
import DOMPurify from "dompurify";
import { useCallback, useEffect, useRef, useState } from "react";
import type { Standardtekst, StandardtekstSearch } from "./Standardtekst.types";
import { standardtekstPluginKey } from "./StandardtekstExtension";

interface StandardtekstModalProps {
    editor: Editor;
    searchStandardtekster: StandardtekstSearch;
}

export function StandardtekstModal({ editor, searchStandardtekster }: StandardtekstModalProps) {
    const open = useEditorState({
        editor,
        selector: ({ editor }) => standardtekstPluginKey.getState(editor.state)?.isOpen ?? false,
    });
    const [query, setQuery] = useState("");
    const [standardtekster, setStandardtekster] = useState<Standardtekst[]>([]);
    const [selectedStandardtekst, setSelectedStandardtekst] = useState<Standardtekst | null>(null);
    const [loading, setLoading] = useState(false);
    const [hasError, setHasError] = useState(false);
    const latestSearch = useRef(0);
    const isOpen = useRef(false);
    const focusEditorAfterClose = useRef(false);

    function closeModal() {
        isOpen.current = false;
        latestSearch.current += 1;
        setLoading(false);
        editor.commands.closeStandardtekstModal();
    }

    const search = useCallback(
        async (query: string) => {
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
        },
        [searchStandardtekster],
    );

    useEffect(() => {
        if (!open) return;

        isOpen.current = true;
        focusEditorAfterClose.current = false;
        setQuery("");
        setSelectedStandardtekst(null);
        void search("");

        return () => {
            isOpen.current = false;
            latestSearch.current += 1;
        };
    }, [open, search]);

    return (
        <Dialog
            open={open}
            onOpenChange={(nextOpen) => {
                if (nextOpen) {
                    editor.commands.openStandardtekstModal();
                } else {
                    closeModal();
                }
            }}
        >
            <Dialog.Popup
                width="large"
                returnFocusTo={() =>
                    focusEditorAfterClose.current && !editor.isDestroyed && editor.isInitialized
                        ? editor.view.dom
                        : undefined
                }
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
                                                        <Tag key={tag} size="small" variant={"moderate"}>
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
                            if (editor.chain().focus().insertStandardtekst(selectedStandardtekst.html).run()) {
                                focusEditorAfterClose.current = true;
                                isOpen.current = false;
                                latestSearch.current += 1;
                                setLoading(false);
                            }
                        }}
                    >
                        Sett inn tekst
                    </Button>
                </Dialog.Footer>
            </Dialog.Popup>
        </Dialog>
    );
}
