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

    it("beholder opprinnelige overskriftsnivåer og formatering ved gjentatt uttak", () => {
        const innhold = "<h2>Vedtak</h2><p>Tekst</p><h4><strong>Begrunnelse</strong> <mark>viktig</mark></h4>";
        let html = `<section><div class="data-editable">${innhold}</div></section>`;

        for (let runde = 0; runde < 3; runde++) {
            const redigerbart = finnRedigerbartInnhold(html);
            const doc = new DOMParser().parseFromString(redigerbart, "text/html");

            expect(Array.from(doc.querySelectorAll("h1, h2, h3, h4, h5, h6"), (el) => el.tagName)).toEqual([
                "H2",
                "H4",
            ]);
            expect(doc.querySelector("h4 strong")?.textContent).toBe("Begrunnelse");
            expect(doc.querySelector("h4 mark")?.textContent).toBe("viktig");
            html = `<section><div class="data-editable">${doc.body.innerHTML}</div></section>`;
        }
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
