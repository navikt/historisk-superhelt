import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { useAutoSave } from "./useAutosave";

describe("useAutoSave", () => {
    it("does not call saveFunction with initial value", () => {
        vi.useFakeTimers();
        const save = vi.fn();

        renderHook(() => useAutoSave("initial", save, 500));
        act(() => { vi.advanceTimersByTime(1000); });

        expect(save).not.toHaveBeenCalled();
        vi.useRealTimers();
    });

    it("calls saveFunction after debounce when value changes", () => {
        vi.useFakeTimers();
        const save = vi.fn();

        const { rerender } = renderHook(({ value }) => useAutoSave(value, save, 500), {
            initialProps: { value: "initial" },
        });

        rerender({ value: "changed" });
        act(() => { vi.advanceTimersByTime(500); });

        expect(save).toHaveBeenCalledWith("changed");
        expect(save).toHaveBeenCalledTimes(1);
        vi.useRealTimers();
    });

    it("does not call saveFunction before debounce completes", () => {
        vi.useFakeTimers();
        const save = vi.fn();

        const { rerender } = renderHook(({ value }) => useAutoSave(value, save, 500), {
            initialProps: { value: "initial" },
        });

        rerender({ value: "changed" });
        act(() => { vi.advanceTimersByTime(300); });

        expect(save).not.toHaveBeenCalled();
        vi.useRealTimers();
    });

    it("only saves the latest value on rapid changes", () => {
        vi.useFakeTimers();
        const save = vi.fn();

        const { rerender } = renderHook(({ value }) => useAutoSave(value, save, 500), {
            initialProps: { value: "initial" },
        });

        rerender({ value: "change1" });
        act(() => { vi.advanceTimersByTime(200); });

        rerender({ value: "change2" });
        act(() => { vi.advanceTimersByTime(500); });

        expect(save).toHaveBeenCalledWith("change2");
        expect(save).toHaveBeenCalledTimes(1);
        vi.useRealTimers();
    });
});
