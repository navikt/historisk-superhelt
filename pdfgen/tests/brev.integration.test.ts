import { expect, test } from "vitest";
import { getDocument } from "pdfjs-dist/legacy/build/pdf.mjs";
import { type Brevdata, brevdata } from "./brev-data";

const baseURL = process.env.PDFGEN_URL ?? "http://localhost:8086";

async function request(data: Brevdata) {
    return fetch(`${baseURL}/api/v1/genpdf/superhelt/brev`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
        signal: AbortSignal.timeout(30000),
    });
}

for (const [name, content, expected] of [
    ["tomt innhold", "", [1, 1, 1, 1, 1]],
    ["uten overskrifter", "Vanlig tekst.", [1, 1, 1, 1, 1]],
    ["første H2", "== Første\n\nTekst.", [1, 1, 1, 1, 1, 1]],
    ["nivåhopp", "= Første\n\n==== Andre\n\nTekst.", [1, 2, 2, 2, 2, 2, 2]],
    ["eksplisitte heading-kall med nivåhopp", "#heading(depth: 2)[Første]\n\n#heading(depth: 4)[Andre]\n\n#heading(depth: 2)[Tredje]", [1, 2, 1, 1, 1, 1, 1, 1]],
    ["avsnitt før H1", "Innledning.\n\n= Første\n\nTekst.", [1, 2, 2, 2, 2, 2]],
    ["langt brev", "== Første\n\n" + "Et avsnitt for å teste sideskift.\n\n".repeat(200), [1, 1, 1, 1, 1, 1]],
] as const) {
    test(`PDF med ${name} har gyldig hierarki og sidetall`, async () => {
        const response = await request(brevdata({ innhold_typst: content }));
        expect(response.status, response.ok ? "" : await response.text()).toBe(200);
        expect(response.headers.get("content-type")).toMatch(/application\/pdf/);
        const task = getDocument({ data: new Uint8Array(await response.arrayBuffer()) });
        try {
            const pdf = await task.promise;
            const headings: number[] = [];
            const texts: string[] = [];
            function visit(node: { role?: string; children?: object[] } | null) {
                if (node?.role && /^H\d+$/.test(node.role)) headings.push(Number(node.role.slice(1)));
                node?.children?.forEach(visit);
            }
            for (let number = 1; number <= pdf.numPages; number++) {
                const page = await pdf.getPage(number);
                visit(await page.getStructTree());
                const text = (await page.getTextContent()).items.map((it) => "str" in it ? it.str : "").join(" ");
                expect(text, "Hver side må ha riktig sidetall").toContain(`side ${number} av ${pdf.numPages}`);
                texts.push(text);
            }
            expect(headings).toEqual(expected);
            expect(texts.join(" ")).toContain("Test Saksbehandler");
            expect(texts.join(" ")).toContain("Test Beslutter");
            if (name === "langt brev") expect(pdf.numPages).toBeGreaterThan(2);
        } finally {
            await task.destroy();
        }
    });
}

test("samhandlerbrev uten overskrifter har ingen standardtekster eller besluttersignatur i fritekst", async () => {
    const response = await request(brevdata({ brevtype: "BREV", mottaker: "SAMHANDLER", innhold_typst: "Vanlig tekst." }));
    expect(response.status, response.ok ? "" : await response.text()).toBe(200);
    const task = getDocument({ data: new Uint8Array(await response.arrayBuffer()) });
    try {
        const pdf = await task.promise;
        const page = await pdf.getPage(1);
        const text = (await page.getTextContent()).items.map((it) => "str" in it ? it.str : "").join(" ");
        expect(text).toContain("Test Saksbehandler");
        expect(text).not.toContain("Test Beslutter");
        expect(text).not.toContain("Du kan klage");
        expect(pdf.numPages).toBe(1);
    } finally {
        await task.destroy();
    }
});

test("eksplisitte avsnitt beholder PDF-layout og gir P-tagger i listepunkter", async () => {
    const variants = [
        `Første avsnitt.

Andre avsnitt.

#list(tight: true,
[Hovedpunkt
#enum(tight: true, [Underpunkt])

Fortsettelse],
[Neste punkt],
)`,
        `#par[Første avsnitt.]#par[Andre avsnitt.]

#list(tight: true,
[#par[Hovedpunkt]
#enum(tight: true, [#par[Underpunkt]])
#par[Fortsettelse]],
[#par[Neste punkt]],
)`,
    ];
    const layouts = [];
    for (const [index, innhold_typst] of variants.entries()) {
        const response = await request(brevdata({ brevtype: "BREV", mottaker: "SAMHANDLER", innhold_typst }));
        expect(response.status, response.ok ? "" : await response.text()).toBe(200);
        const task = getDocument({ data: new Uint8Array(await response.arrayBuffer()) });
        try {
            const pdf = await task.promise;
            const pages = [];
            let paragraphsInLists = 0;
            function visit(node: { role?: string; children?: object[] } | null, inList = false) {
                const nested = inList || node?.role === "LI";
                if (nested && node?.role === "P") paragraphsInLists++;
                node?.children?.forEach((child) => visit(child, nested));
            }
            for (let number = 1; number <= pdf.numPages; number++) {
                const page = await pdf.getPage(number);
                visit(await page.getStructTree());
                pages.push((await page.getTextContent()).items
                    .filter((item) => "str" in item && item.str.trim())
                    .map((item) => {
                        if (!("str" in item)) throw new Error("Forventet tekst");
                        return { text: item.str, transform: item.transform, width: item.width, height: item.height };
                    }));
            }
            layouts.push(pages);
            if (index === 1) expect(paragraphsInLists).toBe(4);
        } finally {
            await task.destroy();
        }
    }
    expect(layouts[1]).toEqual(layouts[0]);
});

test("ugyldig innhold returnerer Problem Details, ikke PDF", async () => {
    const response = await request(brevdata({ innhold_typst: "#[" }));
    expect(response.status).toBe(500);
    expect(response.headers.get("content-type")).toMatch(/application\/problem\+json/);
    const problem = await response.json();
    expect(problem.type).toBe("urn:pdfgenrs:error:generation-failed");
    expect(problem.detail).toBeTruthy();
});
