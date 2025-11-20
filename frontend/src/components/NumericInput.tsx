import {TextField, TextFieldProps} from "@navikt/ds-react";
import {useEffect, useState} from "react";

interface NumericInputProps extends Omit<TextFieldProps, "value" | "onChange"> {
    value: number | undefined;
    onChange: (value: number | undefined) => void;
    error?: string | undefined;
}

export function NumericInput(props: NumericInputProps) {

    const {value, onChange, error} = props;
    const [stringValue, setStringValue] = useState<string | undefined>(value ? value.toString() : undefined);
    const [numericError, setNumericError] = useState<string | undefined>();

    // Sync stringValue with value prop changes
    useEffect(() => {
        setStringValue(value ? value.toString() : undefined)
    }, [value]);


    const changeStringValue = (inputValue: string) => {
        const trimmedInput = inputValue.trim();
        setStringValue(trimmedInput)
        if (trimmedInput === "") {
            setNumericError(undefined);
            onChange(undefined)
        }
        const numericValue = Number(trimmedInput);
        if (!isNaN(numericValue)) {
            setNumericError(undefined);
            onChange(numericValue);
        } else {
            setNumericError("Vennligst oppgi et gyldig tall");
        }

    };

    return <TextField
        {...props}
        inputMode="numeric"
        value={stringValue ?? ""}
        onChange={e => changeStringValue(e.target.value)}
        error={numericError || error}
    />
}