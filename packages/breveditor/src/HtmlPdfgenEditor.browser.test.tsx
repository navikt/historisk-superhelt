import { Editor } from "@tiptap/react";
import StarterKit from "@tiptap/starter-kit";
import { act, useState } from "react";
import { describe, expect, test, vi } from "vitest";
import { page, userEvent } from "vitest/browser";
import { render } from "vitest-browser-react";
import { HtmlPdfgenEditor } from "./HtmlPdfgenEditor";
import { MenuBar } from "./MenuBar";
import { searchStandardtekster } from "./standardtekst/Standardtekst.search";
import type { Standardtekst, StandardtekstResponse } from "./standardtekst/Standardtekst.types";
import { StandardtekstExtension, standardtekstPluginKey } from "./standardtekst/StandardtekstExtension";
import { StandardtekstModal } from "./standardtekst/StandardtekstModal";

describe("HtmlPdfgenEditor", () => {
    const testHtml =
        '<html><body><section><p>Før redigerbart felt</p></section><section><div class="data-editable"><p>Redigerbar tekst</p></div></section><section><p>Etter redigerbart felt</p></section></body></html>';

    function response(
        data: Standardtekst[] | undefined,
        status: Partial<StandardtekstResponse> = {},
    ): StandardtekstResponse {
        return {
            data,
            isPending: false,
            isError: false,
            ...status,
        };
    }

    function renderEditor({
        html = testHtml,
        onChange = vi.fn(),
        ...props
    }: {
        html?: string;
        onChange?: (html: string) => void;
        standardtekster?: StandardtekstResponse;
        onBlur?: () => void;
        error?: string;
        readOnly?: boolean;
        saveStatus?: "idle" | "saving" | "saved" | "error";
        onSave?: () => Promise<void>;
        previewUrl?: string;
    } = {}) {
        render(<HtmlPdfgenEditor html={html} onChange={onChange} {...props} />);
    }

    describe("Innhold og skrivebeskyttelse", () => {
        test("viser fast prefix og redigerbar tekst, og postfix etter åpning", async () => {
            renderEditor();

            await expect.element(page.getByText("Før redigerbart felt")).toBeVisible();
            await expect.element(page.getByText("Redigerbar tekst")).toBeVisible();
            await expect.element(page.getByText("Etter redigerbart felt")).not.toBeVisible();

            await userEvent.click(page.getByRole("button", { name: "Standardtekster og signatur" }));

            await expect.element(page.getByText("Etter redigerbart felt")).toBeVisible();
        });

        test("skriving i editoren kaller onChange med HTML som inneholder ny tekst", async () => {
            const onChange = vi.fn();
            renderEditor({ onChange });

            const editor = page.getByRole("textbox");
            await userEvent.click(editor);
            await userEvent.keyboard("{End} Ny tekst");

            expect(onChange).toHaveBeenCalledWith(expect.stringContaining("Ny tekst"));
        });

        test("read-only skjuler redigeringsknappene og låser editoren", async () => {
            renderEditor({ readOnly: true, previewUrl: "/test-brev.pdf" });

            await expect.element(page.getByRole("button", { name: "Lagre" })).not.toBeInTheDocument();
            await expect
                .element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" }))
                .not.toBeInTheDocument();
            await expect
                .element(page.getByRole("button", { name: "Standardtekster", exact: true }))
                .not.toBeInTheDocument();
            await expect.element(page.getByRole("textbox")).toHaveAttribute("contenteditable", "false");
        });
    });

    describe("Manuell lagring", () => {
        describe("Hurtigtaster", () => {
            test.each([
                { modifier: "Control", name: "Ctrl" },
                { modifier: "Meta", name: "Meta" },
            ])("$name+S kaller onSave én gang", async ({ modifier }) => {
                const onSave = vi.fn(async () => {});
                renderEditor({ onSave });

                await userEvent.click(page.getByRole("textbox"));
                await userEvent.keyboard(`{${modifier}>}s{/${modifier}}`);

                expect(onSave).toHaveBeenCalledTimes(1);
            });

            test("lagringshurtigtasten ikke virker utenfor editoren", () => {
                const onSave = vi.fn(async () => {});
                renderEditor({ onSave });

                const event = new KeyboardEvent("keydown", {
                    key: "s",
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true,
                });
                document.dispatchEvent(event);

                expect(event.defaultPrevented).toBe(false);
                expect(onSave).not.toHaveBeenCalled();
            });

            test("lagringshurtigtasten håndteres ikke uten lagringsfunksjon", () => {
                renderEditor();

                const editor = document.querySelector('[data-testid="tiptapeditor"]');
                const withoutCallback = new KeyboardEvent("keydown", {
                    key: "s",
                    ctrlKey: true,
                    bubbles: true,
                    cancelable: true,
                });
                editor?.dispatchEvent(withoutCallback);

                expect(withoutCallback.defaultPrevented).toBe(false);
            });

            test("lagringshurtigtasten håndteres ikke i read-only-modus", () => {
                const onSave = vi.fn(async () => {});
                renderEditor({ readOnly: true, onSave });
                const readOnlyEditor = document.querySelector('[data-testid="tiptapeditor"]');
                const readOnlyShortcut = new KeyboardEvent("keydown", {
                    key: "s",
                    metaKey: true,
                    bubbles: true,
                    cancelable: true,
                });
                readOnlyEditor?.dispatchEvent(readOnlyShortcut);

                expect(readOnlyShortcut.defaultPrevented).toBe(false);
                expect(onSave).not.toHaveBeenCalled();
            });

            test("lagringshurtigtasten ignorerer Alt- og Shift-kombinasjoner", () => {
                const onSave = vi.fn(async () => {});
                renderEditor({ onSave });

                const editor = document.querySelector('[data-testid="tiptapeditor"]');
                for (const modifier of [{ altKey: true }, { shiftKey: true }]) {
                    const event = new KeyboardEvent("keydown", {
                        key: "s",
                        ctrlKey: true,
                        bubbles: true,
                        cancelable: true,
                        ...modifier,
                    });
                    editor?.dispatchEvent(event);
                    expect(event.defaultPrevented).toBe(false);
                }

                expect(onSave).not.toHaveBeenCalled();
            });
        });
        describe("Knapp, status og oppdatert callback", () => {
            test("lagreknappen kaller onSave én gang uten å kalle onChange", async () => {
                const onChange = vi.fn();
                const onSave = vi.fn(async () => {});
                renderEditor({ onChange, onSave });

                await userEvent.click(page.getByRole("button", { name: "Lagre", exact: true }));

                expect(onSave).toHaveBeenCalledTimes(1);
                expect(onChange).not.toHaveBeenCalled();
            });

            test("lagreknappen er deaktivert uten onSave", async () => {
                renderEditor();

                await expect.element(page.getByRole("button", { name: "Lagre" })).toBeDisabled();
            });

            test("bruker nyeste onSave etter rerender uten å miste editorinnhold", async () => {
                const firstOnSave = vi.fn(async () => {});
                const latestOnSave = vi.fn(async () => {});
                const initialProps = {
                    html: testHtml,
                    onChange: vi.fn(),
                    standardtekster: response([]),
                    onSave: firstOnSave,
                };
                function EditorWithReplaceableSave() {
                    const [saveCallback, setSaveCallback] = useState(() => firstOnSave);
                    return (
                        <>
                            <button type="button" onClick={() => setSaveCallback(() => latestOnSave)}>
                                Bytt lagringsfunksjon
                            </button>
                            <HtmlPdfgenEditor {...initialProps} onSave={saveCallback} />
                        </>
                    );
                }
                render(<EditorWithReplaceableSave />);

                const editor = page.getByRole("textbox");
                await userEvent.click(editor);
                await userEvent.keyboard("{End} Behold dette");
                await userEvent.click(page.getByRole("button", { name: "Bytt lagringsfunksjon" }));
                await userEvent.click(editor);
                await userEvent.keyboard("{Control>}s{/Control}");

                expect(latestOnSave).toHaveBeenCalledTimes(1);
                expect(firstOnSave).not.toHaveBeenCalled();
                await expect.element(page.getByText("Behold dette")).toBeVisible();
            });
        });
    });

    describe("PDF-forhåndsvisning", () => {
        test("viser ikke forhåndsvisningsknappen uten previewUrl", async () => {
            renderEditor();

            await expect
                .element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" }))
                .not.toBeInTheDocument();
        });

        test("venter på lagring før PDF-forhåndsvisning, og Escape lukker dialogen med fokusretur", async () => {
            const onChange = vi.fn();
            let resolveSave: (() => void) | undefined;
            const onSave = vi.fn(
                () =>
                    new Promise<void>((resolve) => {
                        resolveSave = resolve;
                    }),
            );
            const previewUrl = "/test-brev.pdf";
            renderEditor({ onChange, onSave, previewUrl });

            const previewButton = page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" });
            const editor = page.getByRole("textbox");
            await userEvent.click(editor);
            await userEvent.keyboard("{End} Ny tekst");
            await userEvent.click(previewButton);

            expect(onChange).toHaveBeenLastCalledWith(expect.stringContaining("Ny tekst"));
            expect(onSave).toHaveBeenCalledTimes(1);
            expect(onChange.mock.invocationCallOrder.at(-1)).toBeLessThan(onSave.mock.invocationCallOrder[0]);
            await expect.element(page.getByRole("dialog", { name: "Forhåndsvis dokumentet" })).not.toBeInTheDocument();
            resolveSave?.();
            await expect.element(page.getByRole("dialog", { name: "Forhåndsvis dokumentet" })).toBeVisible();
            const embed = page.getByTitle("PDF-forhåndsvisning av brevet");
            await expect.element(embed).toBeVisible();
            await expect.element(embed).toHaveAttribute("src", previewUrl);

            await userEvent.keyboard("{Escape}");
            await expect.element(page.getByRole("dialog", { name: "Forhåndsvis dokumentet" })).not.toBeInTheDocument();
            await expect.element(previewButton).toHaveFocus();
        });
    });

    describe("Standardtekster", () => {
        describe("Søk, åpning og innsetting", () => {
            test("skjuler velgeren når responsen mangler ved montering", async () => {
                renderEditor();

                await expect
                    .element(page.getByRole("button", { name: "Standardtekster", exact: true }))
                    .not.toBeInTheDocument();

                await userEvent.click(page.getByRole("textbox"));
                await userEvent.keyboard("{Control>}{Shift>}y{/Shift}{/Control}");

                await expect
                    .element(page.getByRole("dialog", { name: "Sett inn standardtekst" }))
                    .not.toBeInTheDocument();
            });

            test("søker i tittel, HTML og tags uten å kjenne sakstypefiltrering", async () => {
                const catalog: Standardtekst[] = [
                    {
                        id: "general",
                        title: "Utbetaling",
                        html: "<p>Generell tekst</p>",
                        tags: ["betaling"],
                        target: "general",
                    },
                    {
                        id: "parykk",
                        title: "Innvilgelse",
                        html: "<p>Hodeform</p>",
                        tags: ["Parykk", "avvikende hodeform"],
                        target: "stondad",
                    },
                    {
                        id: "bryst",
                        title: "Innvilgelse",
                        html: "<p>Brystprotese</p>",
                        tags: ["Brystprotese"],
                        target: "stondad",
                    },
                ];
                const caseTekster = catalog.filter((item) => item.id !== "bryst");
                renderEditor({ standardtekster: response(caseTekster) });

                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
                await expect.element(page.getByRole("button", { name: "Utbetaling" })).toBeVisible();
                await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
                await userEvent.click(page.getByRole("button", { name: "Utbetaling" }));
                await expect.element(page.getByText("betaling", { exact: true })).toBeVisible();
                await userEvent.click(page.getByRole("button", { name: /Innvilgelse Parykk/ }));
                await expect.element(page.getByText("avvikende hodeform", { exact: true })).toBeVisible();
                await expect.element(page.getByText("Brystprotese")).not.toBeInTheDocument();

                await userEvent.type(page.getByRole("searchbox", { name: "Søk etter standardtekst" }), "hodeform");
                await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
                await expect.element(page.getByRole("button", { name: "Utbetaling" })).not.toBeInTheDocument();
            });

            test("viser standardtekstmodalen fra ikonknappen i menylinja", async () => {
                renderEditor({ standardtekster: response([]) });

                const standardtekstButton = page.getByRole("button", { name: "Standardtekster", exact: true });
                await userEvent.hover(standardtekstButton);
                await expect.element(page.getByRole("tooltip")).toHaveTextContent("Sett inn standardtekster");

                await userEvent.click(standardtekstButton);
                await expect.element(page.getByRole("dialog", { name: "Sett inn standardtekst" })).toBeVisible();
            });

            test("åpner standardtekstmodalen med Ctrl+Shift+Y fra editoren", async () => {
                renderEditor({ standardtekster: response([]) });

                await userEvent.click(page.getByRole("textbox"));
                await userEvent.keyboard("{Control>}{Shift>}y{/Shift}{/Control}");

                await expect.element(page.getByRole("dialog", { name: "Sett inn standardtekst" })).toBeVisible();
            });

            test("viser tomtilstand for en tilgjengelig, tom katalog", async () => {
                renderEditor({ standardtekster: response([]) });

                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));

                await expect.element(page.getByText("Ingen standardtekster funnet.")).toBeVisible();
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
            });

            test("viser data når en ventende respons oppdateres", async () => {
                const standardtekst: Standardtekst = {
                    id: "innvilgelse",
                    title: "Innvilgelse",
                    html: "<p>Innhold</p>",
                    tags: [],
                    target: "general",
                };

                let updateResponse: (response: StandardtekstResponse) => void = () => {};
                function EditorWithUpdatedResponse() {
                    const [responseState, setResponseState] = useState(response([standardtekst], { isPending: true }));
                    updateResponse = setResponseState;
                    return <HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} standardtekster={responseState} />;
                }

                render(<EditorWithUpdatedResponse />);
                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
                await expect.element(page.getByRole("status")).toHaveTextContent("Søker etter standardtekster");
                await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();

                await act(async () => updateResponse(response([standardtekst])));
                await expect.element(page.getByRole("button", { name: standardtekst.title })).toBeVisible();
            });

            test("viser generell feil uten rå feilmelding eller prøv igjen", async () => {
                const standardtekst: Standardtekst = {
                    id: "utdatert",
                    title: "Skal ikke vises",
                    html: "<p>Utdatert</p>",
                    tags: [],
                    target: "general",
                };
                renderEditor({
                    standardtekster: response([standardtekst], {
                        isError: true,
                        error: new Error("intern feildetalj"),
                    }),
                });

                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));

                await expect.element(page.getByText("Standardtekstene kunne ikke lastes inn.")).toBeVisible();
                await expect.element(page.getByText("intern feildetalj")).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: "Prøv igjen" })).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
            });

            test("fjerner valgt tekst når oppdatert respons ikke inneholder den lenger", async () => {
                const standardtekst: Standardtekst = {
                    id: "utdatert",
                    title: "Innvilgelse",
                    html: "<p>Skal ikke settes inn</p>",
                    tags: [],
                    target: "general",
                };

                let updateResponse: (response: StandardtekstResponse) => void = () => {};
                function EditorWithUpdatedResponse() {
                    const [responseState, setResponseState] = useState(response([standardtekst]));
                    updateResponse = setResponseState;
                    return <HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} standardtekster={responseState} />;
                }

                render(<EditorWithUpdatedResponse />);
                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
                await userEvent.click(page.getByRole("button", { name: standardtekst.title }));
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeEnabled();

                await act(async () => updateResponse(response([])));

                await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
            });

            test("fjerner valgt tekst når søket skjuler den", async () => {
                const standardtekst: Standardtekst = {
                    id: "parykk",
                    title: "Parykk",
                    html: "<p>Stønad</p>",
                    tags: ["Innvilgelse"],
                    target: "stondad",
                };
                renderEditor({ standardtekster: response([standardtekst]) });

                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
                await userEvent.click(page.getByRole("button", { name: standardtekst.title }));
                await userEvent.type(page.getByRole("searchbox", { name: "Søk etter standardtekst" }), "mangler");

                await expect.element(page.getByRole("button", { name: standardtekst.title })).not.toBeInTheDocument();
                await expect.element(page.getByRole("button", { name: "Sett inn tekst" })).toBeDisabled();
            });

            test("prioriterer relevans, saksspesifikke tekster og stabil rekkefølge", () => {
                const catalog: Standardtekst[] = [
                    {
                        id: "generell-innhold",
                        title: "Generell",
                        html: "<p>treff her</p>",
                        tags: [],
                        target: "general",
                    },
                    {
                        id: "generell-tag",
                        title: "Generell",
                        html: "<p>Innhold</p>",
                        tags: ["treff"],
                        target: "general",
                    },
                    {
                        id: "generell-tittel-1",
                        title: "Treff tittel 1",
                        html: "<p>Innhold</p>",
                        tags: [],
                        target: "general",
                    },
                    {
                        id: "saksspesifikk-innhold",
                        title: "Saksspesifikk",
                        html: "<p>treff her</p>",
                        tags: [],
                        target: "stondad",
                    },
                    {
                        id: "saksspesifikk-tittel",
                        title: "Treff tittel",
                        html: "<p>Innhold</p>",
                        tags: [],
                        target: "stondad",
                    },
                    {
                        id: "generell-tittel-2",
                        title: "Treff tittel 2",
                        html: "<p>Innhold</p>",
                        tags: [],
                        target: "general",
                    },
                ];

                expect(searchStandardtekster(catalog, " treff ")).toEqual([
                    catalog[4],
                    catalog[2],
                    catalog[5],
                    catalog[1],
                    catalog[3],
                    catalog[0],
                ]);
                expect(searchStandardtekster(catalog, "")).toEqual([
                    catalog[3],
                    catalog[4],
                    catalog[0],
                    catalog[1],
                    catalog[2],
                    catalog[5],
                ]);
            });

            test("forhåndsviser og setter inn valgt HTML ved markøren med markeringer og onChange", async () => {
                const onChange = vi.fn();
                const standardtekst: Standardtekst = {
                    id: "marked",
                    title: "Innvilgelse parykk",
                    html: "<p>Stønad på <mark>6 265 kroner</mark>.</p>",
                    tags: ["Parykk"],
                    target: "stondad",
                };
                renderEditor({ onChange, standardtekster: response([standardtekst]) });

                const editor = page.getByRole("textbox");
                await userEvent.click(editor);
                await userEvent.keyboard("{Home}");
                await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
                await userEvent.click(page.getByRole("button", { name: standardtekst.title }));

                await expect.element(page.getByText("6 265 kroner")).toBeVisible();
                await userEvent.click(page.getByRole("button", { name: "Sett inn tekst" }));

                await expect.element(editor).toHaveFocus();
                await expect.poll(() => document.getSelection()?.toString()).toBe("Stønad på 6 265 kroner.");
                expect(onChange).toHaveBeenCalledWith(
                    expect.stringContaining("<p>Stønad på <mark>6 265 kroner</mark>.</p><p>Redigerbar tekst</p>"),
                );
            });

            test("håndterer montering og avmontering av editoren med hurtigtasten", async () => {
                const editor = new Editor({
                    element: null,
                    extensions: [StarterKit, StandardtekstExtension],
                });
                const host = document.createElement("div");
                document.body.append(host);
                try {
                    const screen = await render(
                        <>
                            <MenuBar editor={editor} enableStandardTekster={true} />
                            <StandardtekstModal editor={editor} standardtekster={response([])} />
                        </>,
                    );

                    editor.mount(host);
                    editor.view.dom.dispatchEvent(
                        new KeyboardEvent("keydown", { key: "y", ctrlKey: true, shiftKey: true, bubbles: true }),
                    );
                    await expect.element(page.getByRole("dialog", { name: "Sett inn standardtekst" })).toBeVisible();
                    await userEvent.keyboard("{Escape}");
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(false);

                    editor.unmount();
                    editor.mount(host);
                    editor.view.dom.dispatchEvent(
                        new KeyboardEvent("keydown", { key: "y", ctrlKey: true, shiftKey: true, bubbles: true }),
                    );
                    await expect.element(page.getByRole("dialog", { name: "Sett inn standardtekst" })).toBeVisible();
                    await userEvent.keyboard("{Escape}");

                    editor.destroy();
                    await screen.unmount();
                } finally {
                    editor.destroy();
                    host.remove();
                }
            });
        });
        describe("Extension-kommandoer", () => {
            test("kartlegger lagret markering gjennom dokumentendringer og isolerer editorenes tilstand", () => {
                const editor = new Editor({
                    element: document.createElement("div"),
                    extensions: [StarterKit, StandardtekstExtension],
                    content: "<p>abcdef</p>",
                });
                const otherEditor = new Editor({
                    element: document.createElement("div"),
                    extensions: [StarterKit, StandardtekstExtension],
                    content: "<p>separat</p>",
                });

                try {
                    editor.commands.setTextSelection(4);
                    expect(editor.commands.openStandardtekstModal()).toBe(true);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(true);
                    expect(standardtekstPluginKey.getState(otherEditor.state)?.isOpen).toBe(false);

                    editor.commands.insertContentAt(1, "X");
                    editor.commands.setTextSelection(2);
                    editor.commands.insertStandardtekst("<strong>Y</strong>");

                    expect(editor.getText()).toBe("XabcYdef");
                    expect(editor.state.doc.textBetween(editor.state.selection.from, editor.state.selection.to)).toBe(
                        "Y",
                    );
                    expect(standardtekstPluginKey.getState(editor.state)).toEqual({ isOpen: false, bookmark: null });
                    expect(standardtekstPluginKey.getState(otherEditor.state)?.isOpen).toBe(false);
                } finally {
                    editor.destroy();
                    otherEditor.destroy();
                }
            });

            test("erstatter ikke-tom markering og markerer den innsatte teksten", () => {
                const onUpdate = vi.fn();
                const editor = new Editor({
                    element: document.createElement("div"),
                    extensions: [StarterKit, StandardtekstExtension],
                    content: "<p>abcdef</p>",
                    onUpdate,
                });

                try {
                    editor.commands.setTextSelection({ from: 2, to: 5 });
                    editor.commands.openStandardtekstModal();
                    expect(onUpdate).not.toHaveBeenCalled();

                    editor.commands.insertStandardtekst("<strong>XY</strong>");

                    expect(editor.getText()).toBe("aXYef");
                    expect(editor.state.doc.textBetween(editor.state.selection.from, editor.state.selection.to)).toBe(
                        "XY",
                    );
                    expect(editor.getHTML()).toContain("<strong>XY</strong>");
                    expect(onUpdate).toHaveBeenCalledTimes(1);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(false);
                } finally {
                    editor.destroy();
                }
            });

            test("can tester kommandoer uten å endre editor- eller plugin-tilstand", () => {
                const editor = new Editor({
                    element: document.createElement("div"),
                    extensions: [StarterKit, StandardtekstExtension],
                    content: "<p>innhold</p>",
                });

                try {
                    const originalDocument = editor.state.doc;
                    expect(editor.can().openStandardtekstModal()).toBe(true);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(false);

                    editor.commands.openStandardtekstModal();
                    expect(editor.can().insertStandardtekst("<p>nytt</p>")).toBe(true);

                    expect(editor.state.doc).toBe(originalDocument);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(true);
                    expect(editor.can().closeStandardtekstModal()).toBe(true);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(true);
                } finally {
                    editor.destroy();
                }
            });

            test("avviser åpning og innsetting i read-only-modus", () => {
                const editor = new Editor({
                    element: document.createElement("div"),
                    extensions: [StarterKit, StandardtekstExtension],
                    content: "<p>innhold</p>",
                });

                try {
                    editor.setEditable(false);
                    expect(editor.commands.openStandardtekstModal()).toBe(false);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(false);

                    editor.setEditable(true);
                    editor.commands.openStandardtekstModal();
                    editor.setEditable(false);
                    const originalDocument = editor.state.doc;

                    expect(editor.commands.insertStandardtekst("<p>nytt</p>")).toBe(false);
                    expect(editor.state.doc).toBe(originalDocument);
                    expect(standardtekstPluginKey.getState(editor.state)?.isOpen).toBe(true);
                } finally {
                    editor.destroy();
                }
            });
        });
    });
});
