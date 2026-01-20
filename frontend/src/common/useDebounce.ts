import {useEffect, useState} from 'react';

/**
 * Debounce hook to delay updating the value until after a specified delay
 */
function useDebounce<T>(value: T, delay: number): T {
    const [debouncedValue, setDebouncedValue] = useState<T>(value);

    useEffect(() => {
        // Set a timeout to update the debounced value after the specified delay
        const handler = setTimeout(() => {
            setDebouncedValue(value);
        }, delay);

        // Clear the timeout if the value changes before the delay expires
        // This ensures that the debounced value is only updated when the
        // input has truly stopped changing for the specified duration.
        return () => {
            clearTimeout(handler);
        };
    }, [value, delay]); // Re-run effect if value or delay changes

    return debouncedValue;
}

export default useDebounce;