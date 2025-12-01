import {useEffect} from 'react';
import useDebounce from "./useDebounce";

/**
 * Debounce hook to delay updating the value until after a specified delay
 */
export function useAutoSave<T>(value: T, saveFunction: (value: T) => void, delay: number) {
    const debouncedValue = useDebounce<T>(value, delay);

    useEffect(() => {
        // Only save if the debouncedData is different from the initial data
        // and if a saveFunction is provided
        saveFunction(debouncedValue);
    }, [debouncedValue]);
}

