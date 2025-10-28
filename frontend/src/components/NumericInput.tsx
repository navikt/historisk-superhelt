import {TextField, TextFieldProps} from "@navikt/ds-react";
import {useState} from "react";

interface NumericInputProps extends Omit<TextFieldProps, "value" | "onChange"> {
    value: number|undefined;
    onChange: (value: number) => void;
}

export function NumericInput(props: NumericInputProps) {

    const {value, onChange} = props;
    const [stringValue, setStringValue] = useState<string | undefined>(value? value.toString() : undefined);
    const [error, setError] = useState<string | undefined>();


    const changeStringValue = (inputValue: string) => {
        setStringValue(inputValue)
        const numericValue = Number(inputValue);
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