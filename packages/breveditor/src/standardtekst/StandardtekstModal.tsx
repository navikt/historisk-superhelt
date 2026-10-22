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
import { useEffect, useMemo, useRef, useState } from "react";
import { searchStandardtekster } from "./Standardtekst.search";
import type { StandardtekstResponse } from "./Standardtekst.types";
import { standardtekstPluginKey } from "./StandardtekstExtension";

interface StandardtekstModalProps {
    editor: Editor;
    standardtekster?: StandardtekstResponse;
}

export function StandardtekstModal({ editor, standardtekster }: StandardtekstModalProps) {
    const open = useEditorState({
        editor,
        selector: ({ editor }) => standardtekstPluginKey.getState(editor.state)?.isOpen ?? false,
    });
    const [query, setQuery] = useState("");
    const [selectedStandardtekstId, setSelectedStandardtekstId] = useState<string | null>(null);
    const focusEditorAfterClose = useRef(false);

    function closeModal() {
        editor.commands.closeStandardtekstModal();
    }

    const results = useMemo(
        () =>
            standardtekster?.isPending || standardtekster?.isError || !standardtekster?.data
                ? []
                : searchStandardtekster(standardtekster.data, query),
        [standardtekster, query],
    );
    const selectedStandardtekst = results.find((result) => result.id === selectedStandardtekstId);

    useEffect(() => {
        if (!open) return;

        focusEditorAfterClose.current = false;
        setQuery("");
        setSelectedStandardtekstId(null);
    }, [open]);

    useEffect(() => {
        if (selectedStandardtekstId && !selectedStandardtekst) {
            setSelectedStandardtekstId(null);
        }
    }, [selectedStandardtekst, selectedStandardtekstId]);

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
                                setSelectedStandardtekstId(null);
                            }}
                        />
                        {standardtekster?.isPending && (
                            <HStack gap="space-8" align="center" role="status">
                                <Loader size="small" />
                                Søker etter standardtekster …
                            </HStack>
                        )}
                        {standardtekster?.isError && (
                            <ErrorMessage showIcon>Standardtekstene kunne ikke lastes inn.</ErrorMessage>
                        )}
                        {!standardtekster?.isPending && !standardtekster?.isError && results.length === 0 && (
                            <BodyShort>Ingen standardtekster funnet.</BodyShort>
                        )}
                        {results.length > 0 && (
                            <Accordion>
                                {results.map((standardtekst) => (
                                    <Accordion.Item
                                        key={standardtekst.id}
                                        open={selectedStandardtekstId === standardtekst.id}
                                        onOpenChange={(itemOpen) =>
                                            setSelectedStandardtekstId(itemOpen ? standardtekst.id : null)
                                        }
                                    >
                                        <Accordion.Header>
                                            <HStack justify={"space-between"} gap={"space-8"}>
                                                <Label>{standardtekst.title}</Label>
                                                <Tag size="small" variant="outline" data-color="meta-purple">
                                                    {standardtekst.stonadstype ?? "Generell"}
                                                </Tag>
                                                <HStack gap="space-8">
                                                    {standardtekst.tags.map((tag) => (
                                                        <Tag
                                                            key={tag}
                                                            size="small"
                                                            variant="moderate"
                                                            data-color="neutral"
                                                        >
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
