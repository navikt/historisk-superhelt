import path from "node:path";
import { fileURLToPath } from "node:url";
import { expect, test } from "vitest";
import { NodeCompiler } from "@myriaddreamin/typst-ts-node-compiler";
import { JSDOM } from "jsdom";
import { brevdata } from "./brev-data";

const workspace = fileURLToPath(new URL("../", import.meta.url));

function render(data: unknown) {
    const compiler = NodeCompiler.create({
        workspace,
        fontArgs: [{ fontPaths: [path.join(workspace, "fonts")] }],
    });
    compiler.mapShadow(path.join(workspace, "data/superhelt/brev.json"), Buffer.from(JSON.stringify(data)));
    const result = compiler.tryHtml({ mainFilePath: path.join(workspace, "templates/superhelt/brev.typ") });
    if (result.hasError()) {
        const error = result.takeError();
        if (!error) throw new Error("Typst feilet uten diagnostikk");
        throw new Error(JSON.stringify(compiler.fetchDiagnostics(error)));
    }
    expect(result.result, "Malen må produsere HTML").not.toBeNull();
    if (!result.result) throw new Error("Malen produserte ikke HTML");
    return new JSDOM(result.result.html()).window.document;
}

for (const brevtype of ["VEDTAKSBREV", "BREV", "INNHENTINGSBREV", "INFORMASJONSBREV"]) {
    for (const mottaker of ["BRUKER", "SAMHANDLER"]) {
        for (const målform of ["NB", "NN"]) {
            test(`${brevtype}/${mottaker}/${målform} får riktige standardtekster`, () => {
                const doc = render(brevdata({ brevtype, mottaker, målform }));
                const headings = [...doc.querySelectorAll("section:last-child h2")].map((el) => el.textContent);
                const expected = mottaker === "SAMHANDLER" ? [] : [
                    ...(brevtype === "VEDTAKSBREV" ? [
                        "Du kan klage på vedtaket",
                        målform === "NN" ? "Du har rett til innsyn i saka di" : "Du har rett til innsyn i saken din",
                    ] : []),
                    målform === "NN" ? "Du har rettar knytte til personopplysningane dine" : "Du har rettigheter knyttet til personopplysningene dine",
                    målform === "NN" ? "Du har rett til rettleiing frå Nav" : "Du har rett til å få veiledning fra Nav",
                    "Har du spørsmål?",
                ];
                expect(headings).toEqual(expected);
            });
        }
    }
}

for (const brevtype of ["VEDTAKSBREV", "BREV"]) {
    for (const beslutter of ["present", "empty", "null", "missing"]) {
        test(`${brevtype}/${beslutter} får riktig signatur`, () => {
            const data = brevdata({ brevtype });
            if (beslutter === "empty") data.beslutterNavn = "";
            if (beslutter === "null") data.beslutterNavn = null;
            if (beslutter === "missing") delete data.beslutterNavn;
            const doc = render(data);
            const names = [...doc.querySelectorAll(".brev-signaturnavn p")].map((el) => el.textContent);
            expect(names).toEqual(brevtype === "VEDTAKSBREV" && beslutter === "present"
                ? ["Test Beslutter", "Test Saksbehandler"] : ["Test Saksbehandler"]);
            expect(doc.querySelector(".brev-kontor")?.textContent).toBe(data.kontor);
        });
    }
}

test("HTML skiller personalia, redigerbart innhold og standardtekster", () => {
    const doc = render(brevdata({
        innhold_typst: "= Tittel\n\n== Under\n\n=== Detalj\n\n==== Siste\n\n*Fet* _kursiv_ #highlight[markert] #link(\"https://www.nav.no\")[lenke]\n\n- Punkt",
    }));
    expect(doc.querySelectorAll("section")).toHaveLength(3);
    expect(doc.querySelectorAll(".data-editable")).toHaveLength(1);
    const editable = doc.querySelector(".data-editable");
    if (!editable) throw new Error("Mangler redigerbart innhold");
    expect([...editable.querySelectorAll("h1,h2,h3,h4,h5")].map((el) => el.tagName)).toEqual(["H1", "H2", "H3", "H4"]);
    for (const tag of ["strong", "em", "mark", "a", "ul li"]) expect(editable.querySelector(tag)).not.toBeNull();
    expect(editable.querySelector("a")?.getAttribute("href")).toBe("https://www.nav.no");
    expect(editable.textContent).not.toContain("Test Saksbehandler");
    expect(editable.textContent).not.toContain("Du kan klage");
    const prefix = doc.querySelector("section");
    expect(prefix?.querySelector(".brev-personalia")?.textContent).toContain("Test Testesen");
    expect(prefix?.textContent).toContain("TEST-123");
    expect(prefix?.textContent).toContain("14. oktober 2026");
});

test("eksplisitte funksjoner bevarer originalnivåer, formatering og nestede lister i HTML", () => {
    const doc = render(brevdata({
        innhold_typst: `#heading(depth: 2)[Tittel]
#heading(depth: 4)[Detalj]

#par[før#strong[fet]etter#emph[kursiv]slutt#linebreak()Ny linje]

#list(tight: true,
[#par[Hovedpunkt]
#enum(tight: true,
[#par[Underpunkt]],
)

#par[Fortsettelse]],
[#par[Neste punkt]],
)`,
    }));
    const editable = doc.querySelector(".data-editable");
    if (!editable) throw new Error("Mangler redigerbart innhold");
    expect([...editable.querySelectorAll("h1,h2,h3,h4")].map((el) => el.tagName)).toEqual(["H2", "H4"]);
    expect(editable.querySelector("strong")?.textContent).toBe("fet");
    expect(editable.querySelector("em")?.textContent).toBe("kursiv");
    expect(editable.querySelector("br")).not.toBeNull();
    expect(editable.querySelectorAll("ul > li")).toHaveLength(2);
    expect(editable.querySelectorAll("ul > li > ol > li")).toHaveLength(1);
    expect(editable.querySelector("ul > li")?.textContent).toContain("Fortsettelse");
    expect([...editable.querySelectorAll("ul > li > p")].map((el) => el.textContent))
        .toEqual(["Hovedpunkt", "Fortsettelse", "Neste punkt"]);
    expect(editable.querySelector("ol > li > p")?.textContent).toBe("Underpunkt");
});

for (const [html, markup] of [
    ["<p></p>", "#par[]"],
    ["<p>A</p><p></p><p>B</p>", "#par[A]#par[]#par[B]"],
    ["<p></p><p>A</p>", "#par[]#par[A]"],
    ["<p>A</p><p></p>", "#par[A]#par[]"],
    ["<p></p><p></p>", "#par[]#par[]"],
    ["<p>A</p><p></p><p></p><p>B</p>", "#par[A]#par[]#par[]#par[B]"],
] as const) {
    test(`HTML-eksport bevarer avsnittsblokkene i ${html}`, () => {
        const doc = render(brevdata({ innhold_typst: markup }));
        const editable = doc.querySelector(".data-editable");
        if (!editable) throw new Error("Mangler redigerbart innhold");
        const expected = new JSDOM(html).window.document.body;
        expect([...editable.children].map((el) => el.outerHTML))
            .toEqual([...expected.children].map((el) => el.outerHTML));
    });
}

test("HTML-eksport bevarer tomme avsnitt i listepunkter", () => {
    const doc = render(brevdata({
        innhold_typst: "#list(tight: true,\n[#par[A]#par[]#par[B]],\n[#par[]],\n)",
    }));
    const items = [...doc.querySelectorAll(".data-editable ul > li")];
    expect(items.map((item) => [...item.querySelectorAll("p")].map((p) => p.textContent)))
        .toEqual([["A", "", "B"], [""]]);
});

test("tomt dokument gir ingen avsnittsblokker i HTML-eksport", () => {
    const doc = render(brevdata({ innhold_typst: "" }));
    expect(doc.querySelector(".data-editable")?.children).toHaveLength(0);
});

for (const [name, change] of [
    ["manglende innhold", (data: Record<string, unknown>) => { delete data.innhold_typst; }],
    ["feil datatype", (data: Record<string, unknown>) => { data.innhold_typst = 42; }],
    ["ugyldig markup", (data: Record<string, unknown>) => { data.innhold_typst = "#["; }],
] as const) {
    test(`${name} gir eksplisitt renderingsfeil`, () => {
        const data = { ...brevdata() };
        change(data);
        expect(() => render(data)).toThrow(/innhold_typst|unclosed|expected/i);
    });
}
