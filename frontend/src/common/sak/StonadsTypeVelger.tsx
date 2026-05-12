import { UNSAFE_Combobox } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { getKodeverkStonadsTypeOptions } from "~/common/sak/sak.query";
import type { StonadType } from "~/common/sak/sak.types";

interface Props {
    label: string;
    value?: StonadType;
    error?: string;
    onChange: (type: StonadType | undefined) => void;
    name?: string;
    readOnly?: boolean;
}

export function StonadsTypeVelger({ label, value, error, onChange, name = "stonadstype", readOnly }: Props) {
    const { data: stonadsTyper } = useSuspenseQuery(getKodeverkStonadsTypeOptions());
    const selectValue: string = (value ?? "") as string;

    function changeStonadsType(option: string) {
        onChange(option === "" ? undefined : (option as StonadType));
    }
    return (
        <UNSAFE_Combobox
            label={label}
            name={name}
            error={error}
            isMultiSelect={false}
            readOnly={readOnly}
            onToggleSelected={(option) => changeStonadsType(option)}
            options={stonadsTyper.map((bt) => ({ label: bt.navn, value: bt.type }))}
            selectedOptions={
                stonadsTyper?.filter((s) => s.type === selectValue).map((h) => ({ label: h.navn, value: h.type })) ?? []
            }
        />
    );
}
