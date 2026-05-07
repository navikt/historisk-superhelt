import { describe, expect, it } from "vitest";
import { enumkodeTilTekst, formatertValuta, kortNavn, kortSaksnummer } from "./string.utils";

describe("kortSaksnummer", () => {
    it("strips leading zeros", () => {
        expect(kortSaksnummer("SH-000123")).toBe("SH-123");
    });

    it("keeps single digit", () => {
        expect(kortSaksnummer("SH-000001")).toBe("SH-1");
    });

    it("handles no leading zeros", () => {
        expect(kortSaksnummer("SH-123")).toBe("SH-123");
    });
});

describe("kortNavn", () => {
    it("returns full name when two parts", () => {
        expect(kortNavn("Ola Nordmann")).toBe("Ola Nordmann");
    });

    it("returns first and last for three parts", () => {
        expect(kortNavn("Ola Gunnar Nordmann")).toBe("Ola Nordmann");
    });

    it("returns first and last for four parts", () => {
        expect(kortNavn("Ola Gunnar Erik Nordmann")).toBe("Ola Nordmann");
    });

    it("returns single name as-is", () => {
        expect(kortNavn("Ola")).toBe("Ola");
    });

    it("trims whitespace", () => {
        expect(kortNavn("  Ola Gunnar Nordmann  ")).toBe("Ola Nordmann");
    });
});

describe("enumkodeTilTekst", () => {
    it("converts enum with underscore to text with capital first letter", () => {
        expect(enumkodeTilTekst("UNDER_BEHANDLING")).toBe("Under behandling");
    });

    it("returns undefined for undefined input", () => {
        expect(enumkodeTilTekst(undefined)).toBeUndefined();
    });

    it("handles single word", () => {
        expect(enumkodeTilTekst("FERDIG")).toBe("Ferdig");
    });

    it("handles storBokstav=false", () => {
        expect(enumkodeTilTekst("UNDER_BEHANDLING", false)).toBe("under behandling");
    });

    it("handles custom separator", () => {
        expect(enumkodeTilTekst("UNDER-BEHANDLING", true, "-")).toBe("Under behandling");
    });
});

describe("formatertValuta", () => {
    it("formats positive amount", () => {
        const result = formatertValuta(1000);
        expect(result).toContain("1\u00a0000\u00a0kr"); // \u00a0 er non-breaking space
    });

    it("returns nullValue for null", () => {
        expect(formatertValuta(null)).toBe("-");
    });

    it("returns nullValue for undefined", () => {
        expect(formatertValuta(undefined)).toBe("-");
    });

    it("returns nullValue for zero", () => {
        expect(formatertValuta(0)).toBe("-");
    });

    it("returns nullValue for NaN", () => {
        expect(formatertValuta(Number.NaN)).toBe("-");
        expect(formatertValuta(NaN)).toBe("-");
    });

    it("uses custom nullValue", () => {
        expect(formatertValuta(null, "N/A")).toBe("N/A");
    });
});
