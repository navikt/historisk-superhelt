import { describe, expect, it } from "vitest";
import { StepIcon } from "./StepIcon";
import { StepType } from "./StepType";

describe("StepIcon", () => {
    it("returns null for default type", () => {
        const result = StepIcon({ type: StepType.default, usePartialStatus: false });
        expect(result).toBeNull();
    });

    it("returns CheckmarkIcon for success type", () => {
        const result = StepIcon({ type: StepType.success, usePartialStatus: false });
        expect(result).not.toBeNull();
        expect(result?.type).toBeDefined();
    });

    it("returns ExclamationmarkTriangleFillIcon for warning type", () => {
        const result = StepIcon({ type: StepType.warning, usePartialStatus: false });
        expect(result).not.toBeNull();
        expect(result?.type).toBeDefined();
    });

    it("returns XMarkOctagonFillIcon for danger type", () => {
        const result = StepIcon({ type: StepType.danger, usePartialStatus: false });
        expect(result).not.toBeNull();
        expect(result?.type).toBeDefined();
    });

    it("returns div for success with usePartialStatus", () => {
        const result = StepIcon({ type: StepType.success, usePartialStatus: true });
        expect(result).not.toBeNull();
        expect(result?.type).toBe("div");
    });

    it("returns div for danger with usePartialStatus", () => {
        const result = StepIcon({ type: StepType.danger, usePartialStatus: true });
        expect(result).not.toBeNull();
        expect(result?.type).toBe("div");
    });

    it("still returns icon for warning with usePartialStatus", () => {
        const result = StepIcon({ type: StepType.warning, usePartialStatus: true });
        expect(result).not.toBeNull();
        expect(result?.type).not.toBe("div");
    });
});
