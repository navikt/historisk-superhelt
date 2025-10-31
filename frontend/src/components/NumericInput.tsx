import {TextField, TextFieldProps} from "@navikt/ds-react";
import {useEffect, useState} from "react";

interface NumericInputProps extends Omit<TextFieldProps, "value" | "onChange"> {
    value: number | undefined;
    onChange: (value: number| null) => void;
}

export function NumericInput(props: NumericInputProps) {

    const {value, onChange} = props;
    const [stringValue, setStringValue] = useState<string | undefined>(value ? value.toString() : undefined);
    const [error, setError] = useState<string | undefined>();

    // Sync stringValue with value prop changes
    useEffect(() => {
        setStringValue(value ? value.toString() : undefined)
    }, [value]);


    const changeStringValue = (inputValue: string) => {
        const trimmedInput = inputValue.trim();
        setStringValue(trimmedInput)
        if (trimmedInput ==="") {
            setError(undefined);
            onChange(null)
        }
        const numericValue = Number(trimmedInput);
        if (!isNaN(numericValue)) {
            setError(undefined);
            onChange(numericValue);
        } else {
            setError("Vennligst oppgi et gyldig tall");
        }

    };

    return <TextField
        {...props}
        inputMode="numeric"
        value={stringValue}
        onChange={e => changeStringValue(e.target.value)}
        error={error}
    />
}