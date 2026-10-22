import { describe, expect, test } from "vitest";
import { searchStandardtekster } from "./Standardtekst.search";
import type { Standardtekst } from "./Standardtekst.types";

describe("searchStandardtekster", () => {
    test("finner treff i tittel, tags og HTML uten hensyn til store bokstaver", () => {
        const catalog: Standardtekst[] = [
            {
                id: "title",
                title: "Utbetaling",
                html: "<p>Generell tekst</p>",
                tags: [],
            },
            {
                id: "tag",
                title: "Innvilgelse",
                html: "<p>Generell tekst</p>",
                tags: ["Parykk"],
            },
            {
                id: "content",
                title: "Avslag",
                html: "<p>Hodeform</p>",
                tags: [],
            },
            {
                id: "missing",
                title: "Annet",
                html: "<p>Urelatert</p>",
                tags: ["Annet"],
            },
        ];

        expect(searchStandardtekster(catalog, "  UTBETALING ")).toEqual([catalog[0]]);
        expect(searchStandardtekster(catalog, "parykk")).toEqual([catalog[1]]);
        expect(searchStandardtekster(catalog, "hodeform")).toEqual([catalog[2]]);
    });

    test("prioriterer stønadstype, relevans og stabil rekkefølge uten å søke i stønadstype", () => {
        const catalog: Standardtekst[] = [
            {
                id: "generell-innhold",
                title: "Generell",
                html: "<p>treff her</p>",
                tags: [],
            },
            {
                id: "generell-tag",
                title: "Generell",
                html: "<p>Innhold</p>",
                tags: ["treff"],
            },
            {
                id: "generell-tittel-1",
                title: "Treff tittel 1",
                html: "<p>Innhold</p>",
                tags: [],
            },
            {
                id: "saksspesifikk-innhold",
                title: "Saksspesifikk",
                html: "<p>treff her</p>",
                tags: [],
                stonadstype: "Parykk/hodeplagg",
            },
            {
                id: "saksspesifikk-tittel",
                title: "Treff tittel",
                html: "<p>Innhold</p>",
                tags: [],
                stonadstype: "Parykk/hodeplagg",
            },
            {
                id: "generell-tittel-2",
                title: "Treff tittel 2",
                html: "<p>Innhold</p>",
                tags: [],
            },
        ];

        expect(searchStandardtekster(catalog, " treff ")).toEqual([
            catalog[4],
            catalog[3],
            catalog[2],
            catalog[5],
            catalog[1],
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
        expect(searchStandardtekster(catalog, "parykk/hodeplagg")).toEqual([]);
    });

    test("empty stønadstype does not receive priority", () => {
        const catalog: Standardtekst[] = [
            {
                id: "empty-stonadstype",
                title: "Treff med tom stønadstype",
                html: "",
                tags: [],
                stonadstype: "",
            },
            {
                id: "general",
                title: "Treff generell",
                html: "",
                tags: [],
            },
        ];

        expect(searchStandardtekster(catalog, "treff")).toEqual(catalog);
    });
});
