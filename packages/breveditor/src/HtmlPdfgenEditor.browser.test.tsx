import { expect, test, vi } from "vitest";
import { page, userEvent } from "vitest/browser";
import { render } from "vitest-browser-react";
import { HtmlPdfgenEditor } from "./HtmlPdfgenEditor";

const testHtml =
    '<html><body><section><p>Før redigerbart felt</p></section><section><div class="data-editable"><p>Redigerbar tekst</p></div></section><section><p>Etter redigerbart felt</p></section></body></html>';

test("viser fast prefix og redigerbar tekst, og postfix etter åpning", async () => {
    render(<HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} />);

    await expect.element(page.getByText("Før redigerbart felt")).toBeVisible();
    await expect.element(page.getByText("Redigerbar tekst")).toBeVisible();
    await expect.element(page.getByText("Etter redigerbart felt")).not.toBeVisible();

    await userEvent.click(page.getByRole("button", { name: "Standardtekster og signatur" }));

    await expect.element(page.getByText("Etter redigerbart felt")).toBeVisible();
});

test("skriving i editoren kaller onChange med HTML som inneholder ny tekst", async () => {
    const onChange = vi.fn();
    render(<HtmlPdfgenEditor html={testHtml} onChange={onChange} />);

    const editor = page.getByRole("textbox");
    await userEvent.click(editor);
    await userEvent.keyboard("{End} Ny tekst");

    expect(onChange).toHaveBeenCalledWith(expect.stringContaining("Ny tekst"));
});

test("Ctrl+S kaller onSave én gang", async () => {
    const onSave = vi.fn(async () => {});
    render(<HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} onSave={onSave} />);

    await userEvent.keyboard("{Control>}s{/Control}");

    expect(onSave).toHaveBeenCalledTimes(1);
});

test("read-only skjuler lagring og forhåndsvisning og låser editoren", async () => {
    render(<HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} readOnly previewUrl="/test-brev.pdf" />);

    await expect.element(page.getByRole("button", { name: "Lagre" })).not.toBeInTheDocument();
    await expect.element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" })).not.toBeInTheDocument();
    await expect.element(page.getByRole("textbox")).toHaveAttribute("contenteditable", "false");
});

test("viser ikke forhåndsvisningsknappen uten previewUrl", async () => {
    render(<HtmlPdfgenEditor html={testHtml} onChange={vi.fn()} />);

    await expect.element(page.getByRole("button", { name: "Forhåndsvis dokumentet som PDF" })).not.toBeInTheDocument();
});

test("lagrer og viser PDF-forhåndsvisning, og Escape lukker dialogen med fokusretur", async () => {
    const onChange = vi.fn();
    const onSave = vi.fn(async () => {});
    const previewUrl = "/test-brev.pdf";
    render(<HtmlPdfgenEditor html={testHtml} onChange={onChange} onSave={onSave} previewUrl={previewUrl} />);

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
