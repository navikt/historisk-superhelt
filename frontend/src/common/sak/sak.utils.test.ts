import { describe, expect, it } from "vitest";
import type { Sak } from "@generated";
import { isSakFerdig, utbetalingText, vedtakAvslatt } from "./sak.utils";

function createSak(overrides: Partial<Sak> = {}): Sak {
    return {
        saksnummer: "SH-000001",
        status: "UNDER_BEHANDLING",
        vedtaksResultat: "INNVILGET",
        type: "PARYKK",
        utbetalingsType: "BRUKER",
        rettigheter: [],
        klasseKode: "PARYKK",
        ...overrides,
    } as Sak;
}

describe("isSakFerdig", () => {
    it("returns true for FERDIG status", () => {
        expect(isSakFerdig(createSak({ status: "FERDIG" }))).toBe(true);
    });

    it("returns true for FEILREGISTRERT status", () => {
        expect(isSakFerdig(createSak({ status: "FEILREGISTRERT" }))).toBe(true);
    });

    it("returns false for UNDER_BEHANDLING status", () => {
        expect(isSakFerdig(createSak({ status: "UNDER_BEHANDLING" }))).toBe(false);
    });
});

describe("vedtakAvslatt", () => {
    it("returns true for AVSLATT", () => {
        expect(vedtakAvslatt(createSak({ vedtaksResultat: "AVSLATT" }))).toBe(true);
    });

    it("returns true for HENLAGT", () => {
        expect(vedtakAvslatt(createSak({ vedtaksResultat: "HENLAGT" }))).toBe(true);
    });

    it("returns false for INNVILGET", () => {
        expect(vedtakAvslatt(createSak({ vedtaksResultat: "INNVILGET" }))).toBe(false);
    });
});

describe("utbetalingText", () => {
    it("returns text for BRUKER", () => {
        expect(utbetalingText("BRUKER")).toBe("Utbetaling til bruker");
    });

    it("returns text for FORHANDSTILSAGN", () => {
        expect(utbetalingText("FORHANDSTILSAGN")).toBe("Forhåndstilsagn");
    });

    it("returns default text for unknown type", () => {
        // biome-ignore lint/suspicious/noExplicitAny: testing unknown type
        expect(utbetalingText("UNKNOWN" as any)).toBe("Ingen utbetaling er valgt");
    });
});
