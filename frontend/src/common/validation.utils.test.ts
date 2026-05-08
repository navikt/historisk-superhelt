import { describe, expect, it } from "vitest";
import { hasSize, isValidFnr } from "./validation.utils";

describe("isValidFnr", () => {
    it("returns true for valid 11-digit fnr", () => {
        expect(isValidFnr("12345678901")).toBe(true);
    });

    it("returns false for too short", () => {
        expect(isValidFnr("1234567890")).toBe(false);
    });

    it("returns false for too long", () => {
        expect(isValidFnr("123456789012")).toBe(false);
    });

    it("returns false for non-numeric", () => {
        expect(isValidFnr("1234567890a")).toBe(false);
    });

    it("returns false for undefined", () => {
        expect(isValidFnr(undefined)).toBe(false);
    });

    it("returns false for empty string", () => {
        expect(isValidFnr("")).toBe(false);
    });

    it("returns false for whitespace only", () => {
        expect(isValidFnr("           ")).toBe(false);
    });
});

describe("hasSize", () => {
    it("returns true when value has content and no constraints", () => {
        expect(hasSize("hello")).toBe(true);
    });

    it("returns false for undefined", () => {
        expect(hasSize(undefined)).toBe(false);
    });

    it("returns false for empty string", () => {
        expect(hasSize("")).toBe(false);
    });

    it("returns true when length meets min", () => {
        expect(hasSize("hello", 5)).toBe(true);
    });

    it("returns false when length below min", () => {
        expect(hasSize("hi", 5)).toBe(false);
    });

    it("returns true when length within max", () => {
        expect(hasSize("hello", undefined, 10)).toBe(true);
    });

    it("returns false when length exceeds max", () => {
        expect(hasSize("hello world", undefined, 5)).toBe(false);
    });

    it("returns true when within both min and max", () => {
        expect(hasSize("hello", 3, 10)).toBe(true);
    });
});
