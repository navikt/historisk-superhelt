import { describe, expect, it } from "vitest";
import { dateTilIsoDato, formatIsoDateToDayDotMonthDotYear, isoTilLokal, isoTilLokalTid } from "./dato.utils";

describe("isoTilLokal", () => {
    it("returns empty string for undefined", () => {
        expect(isoTilLokal(undefined)).toBe("");
    });

    it("returns empty string for null", () => {
        expect(isoTilLokal(null)).toBe("");
    });

    it("formats ISO date to Norwegian locale", () => {
        const result = isoTilLokal("2024-03-15");
        expect(result).toContain("15. mars 2024");
    });
});

describe("isoTilLokalTid", () => {
    it("returns empty string for undefined", () => {
        expect(isoTilLokalTid(undefined)).toBe("");
    });

    it("returns empty string for null", () => {
        expect(isoTilLokalTid(null)).toBe("");
    });

    it("formats ISO datetime with time", () => {
        const result = isoTilLokalTid("2024-03-15T10:30:00");
        expect(result).toContain("15. mars 2024 kl. 10:30");
    });
});

describe("dateTilIsoDato", () => {
    it("returns undefined for undefined", () => {
        expect(dateTilIsoDato(undefined)).toBeUndefined();
    });

    it("formats Date to ISO string", () => {
        expect(dateTilIsoDato(new Date(2024, 2, 15))).toBe("2024-03-15");
    });

    it("pads month and day with zeros", () => {
        expect(dateTilIsoDato(new Date(2024, 0, 5))).toBe("2024-01-05");
    });
});

describe("formatIsoDateToDayDotMonthDotYear", () => {
    it("formats valid ISO date", () => {
        expect(formatIsoDateToDayDotMonthDotYear("2024-03-15")).toBe("15.03.2024");
    });

    it("returns empty string for invalid format", () => {
        expect(formatIsoDateToDayDotMonthDotYear("15-03-2024")).toBe("");
    });

    it("returns empty string for non-date string", () => {
        expect(formatIsoDateToDayDotMonthDotYear("not-a-date")).toBe("");
    });

    it("returns empty string for empty string", () => {
        expect(formatIsoDateToDayDotMonthDotYear("")).toBe("");
    });
});
