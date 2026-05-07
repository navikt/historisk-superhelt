import { act, renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import useDebounce from "./useDebounce";

describe("useDebounce", () => {
    it("returns initial value immediately", () => {
        vi.useFakeTimers();
        const { result } = renderHook(() => useDebounce("hello", 500));
        expect(result.current).toBe("hello");
        vi.useRealTimers();
    });

    it("does not update value before delay", () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(({ value, delay }) => useDebounce(value, delay), {
            initialProps: { value: "hello", delay: 500 },
        });

        rerender({ value: "world", delay: 500 });
        act(() => { vi.advanceTimersByTime(300); });

        expect(result.current).toBe("hello");
        vi.useRealTimers();
    });

    it("updates value after delay", () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(({ value, delay }) => useDebounce(value, delay), {
            initialProps: { value: "hello", delay: 500 },
        });

        rerender({ value: "world", delay: 500 });
        act(() => { vi.advanceTimersByTime(500); });

        expect(result.current).toBe("world");
        vi.useRealTimers();
    });

    it("resets timer on rapid changes", () => {
        vi.useFakeTimers();
        const { result, rerender } = renderHook(({ value, delay }) => useDebounce(value, delay), {
            initialProps: { value: "a", delay: 300 },
        });

        rerender({ value: "b", delay: 300 });
        act(() => { vi.advanceTimersByTime(200); });

        rerender({ value: "c", delay: 300 });
        act(() => { vi.advanceTimersByTime(200); });

        expect(result.current).toBe("a");

        act(() => { vi.advanceTimersByTime(100); });
        expect(result.current).toBe("c");

        vi.useRealTimers();
    });
});
