import { describe, expect, test, vi } from "vitest";
import { page, userEvent } from "vitest/browser";
import { render } from "vitest-browser-react";
import { HtmlPdfgenEditor, type Standardtekst, type StandardtekstSearchFilter } from "./HtmlPdfgenEditor";

describe("HtmlPdfgenEditor", () => {
    const testHtml =
        '<html><body><section><p>Før redigerbart felt</p></section><section><div class="data-editable"><p>Redigerbar tekst</p></div></section><section><p>Etter redigerbart felt</p></section></body></html>';

    function renderEditor({
        html = testHtml,
        onChange = vi.fn(),
        searchStandardtekster = async () => [],
        ...props
    }: {
        html?: string;
        onChange?: (html: string) => void;
        searchStandardtekster?: (filter: StandardtekstSearchFilter) => Promise<Standardtekst[]>;
        onBlur?: () => void;
        error?: string;
        readOnly?: boolean;
        saveStatus?: "idle" | "saving" | "saved" | "error";
        onSave?: () => Promise<void>;
        previewUrl?: string;
    } = {}) {
        render(
            <HtmlPdfgenEditor
                html={html}
                onChange={onChange}
                searchStandardtekster={searchStandardtekster}
                {...props}
            />,
        );
    }

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

    test("Ctrl+S kaller onSave én gang", async () => {
        const onSave = vi.fn(async () => {});
        renderEditor({ onSave });

        await userEvent.keyboard("{Control>}s{/Control}");

        expect(onSave).toHaveBeenCalledTimes(1);
    });

    test("read-only skjuler lagring og forhåndsvisning og låser editoren", async () => {
        renderEditor({ readOnly: true, previewUrl: "/test-brev.pdf" });

        await expect.element(page.getByRole("button", { name: "Lagre" })).not.toBeInTheDocument();
        await expect
            .element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" }))
            .not.toBeInTheDocument();
        await expect.element(page.getByRole("textbox")).toHaveAttribute("contenteditable", "false");
    });

    test("viser ikke forhåndsvisningsknappen uten previewUrl", async () => {
        renderEditor();

        await expect
            .element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" }))
            .not.toBeInTheDocument();
    });

    test("lagrer og viser PDF-forhåndsvisning, og Escape lukker dialogen med fokusretur", async () => {
        const onChange = vi.fn();
        const onSave = vi.fn(async () => {});
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
        await expect.element(page.getByRole("dialog", { name: "Forhåndsvis dokumentet" })).toBeVisible();
        const embed = page.getByTitle("PDF-forhåndsvisning av brevet");
        await expect.element(embed).toBeVisible();
        await expect.element(embed).toHaveAttribute("src", previewUrl);

        await userEvent.keyboard("{Escape}");
        await expect.element(page.getByRole("dialog", { name: "Forhåndsvis dokumentet" })).not.toBeInTheDocument();
        await expect.element(previewButton).toHaveFocus();
    });

    test("søker i tittel, HTML og tags uten å kjenne sakstypefiltrering", async () => {
        const catalog: Standardtekst[] = [
            { id: "general", title: "Utbetaling", html: "<p>Generell tekst</p>", tags: ["betaling"] },
            {
                id: "parykk",
                title: "Innvilgelse",
                html: "<p>Hodeform</p>",
                tags: ["Parykk", "avvikende hodeform"],
            },
            {
                id: "bryst",
                title: "Innvilgelse",
                html: "<p>Brystprotese</p>",
                tags: ["Brystprotese"],
            },
        ];
        const caseTekster = catalog.filter((item) => item.id !== "bryst");
        const searchStandardtekster = vi.fn(async ({ query }: StandardtekstSearchFilter) =>
            caseTekster.filter((item) =>
                [item.title, item.html, ...item.tags].join(" ").toLowerCase().includes(query.toLowerCase()),
            ),
        );
        renderEditor({ searchStandardtekster });

        await userEvent.click(page.getByRole("button", { name: "Standardtekster", exact: true }));
        await expect.element(page.getByRole("button", { name: "Utbetaling" })).toBeVisible();
        await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
        await userEvent.click(page.getByRole("button", { name: "Utbetaling" }));
        await expect.element(page.getByText("betaling", { exact: true })).toBeVisible();
        await userEvent.click(page.getByRole("button", { name: /Innvilgelse Parykk/ }));
        await expect.element(page.getByText("avvikende hodeform", { exact: true })).toBeVisible();
        await expect.element(page.getByText("Brystprotese")).not.toBeInTheDocument();
        expect(searchStandardtekster).toHaveBeenCalledWith({ query: "" });

        await userEvent.type(page.getByRole("searchbox", { name: "Søk etter standardtekst" }), "hodeform");
        await expect.element(page.getByRole("button", { name: /Innvilgelse Parykk/ })).toBeVisible();
        await expect.element(page.getByRole("button", { name: "Utbetaling" })).not.toBeInTheDocument();
        expect(searchStandardtekster).toHaveBeenLastCalledWith({ query: "hodeform" });
    });

    test("forhåndsviser og setter inn valgt HTML ved markøren med markeringer og onChange", async () => {
        const onChange = vi.fn();
        const standardtekst: Standardtekst = {
            id: "marked",
            title: "Innvilgelse parykk",
            html: "<p>Stønad på <mark>6 265 kroner</mark>.</p>",
            tags: ["Parykk"],
        };
        renderEditor({ onChange, searchStandardtekster: async () => [standardtekst] });

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

    test("viser ikke standardtekstknappen i read-only-modus", async () => {
        renderEditor({ readOnly: true });

        await expect
            .element(page.getByRole("button", { name: "Standardtekster", exact: true }))
            .not.toBeInTheDocument();
    });
});
