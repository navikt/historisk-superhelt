import { useEffect, useRef } from "react";
import useDebounce from "./useDebounce";

/**
 * Debounce hook to delay updating the value until after a specified delay
 */
export function useAutoSave<T>(value: T, saveFunction: (value: T) => void, delay: number) {
    const debouncedValue = useDebounce<T>(value, delay);
    const initialValue = useRef(value);

    // biome-ignore lint/correctness/useExhaustiveDependencies: Fungerer foreløpig ikke med useDebounce, så vi ignorerer denne regelen her
    useEffect(() => {
        if (debouncedValue === initialValue.current) {
            return;
        }
        saveFunction(debouncedValue);
    }, [debouncedValue]);
}
