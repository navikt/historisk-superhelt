import { describe, expect, it } from "vitest";
import { finnRedigerbartInnhold, toXhtml, utledPostfixInnhold, utledPrefiksInnhold } from "./pdfgen.utils";

const brevHtml = `
    <html>
        <body>
            <section><p>Før redigerbart felt</p></section>
            <section><div class="data-editable"><p>Redigerbar tekst</p></div></section>
            <section><p>Etter redigerbart felt</p></section>
        </body>
    </html>
`;

describe("PDF-gen HTML-uttak", () => {
    it("finner redigerbart innhold fra data-editable", () => {
        const innhold = finnRedigerbartInnhold(brevHtml);

        expect(innhold).toContain("Redigerbar tekst");
        expect(innhold).not.toContain("Før redigerbart felt");
        expect(innhold).not.toContain("Etter redigerbart felt");
    });

    it("håndterer manglende redigerbart felt og skjulte seksjoner", () => {
        expect(finnRedigerbartInnhold("<p>Vanlig tekst</p>")).toBe(toXhtml(""));

        const html =
            '<section data-hidden="true"><p>Skjult tekst</p></section>' +
            '<section><div class="data-editable">Redigerbar tekst</div></section>';
        const prefiks = utledPrefiksInnhold(toXhtml(html));

        expect(prefiks).not.toContain("Skjult tekst");
    });

    it("utleder innholdet før det redigerbare feltet", () => {
        const prefiks = utledPrefiksInnhold(toXhtml(brevHtml));

        expect(prefiks).toContain("Før redigerbart felt");
        expect(prefiks).not.toContain("Redigerbar tekst");
        expect(prefiks).not.toContain("Etter redigerbart felt");
    });

    it("utleder innholdet etter det redigerbare feltet", () => {
        const postfix = utledPostfixInnhold(toXhtml(brevHtml));

        expect(postfix).toContain("Etter redigerbart felt");
        expect(postfix).not.toContain("Redigerbar tekst");
        expect(postfix).not.toContain("Før redigerbart felt");
    });
});
