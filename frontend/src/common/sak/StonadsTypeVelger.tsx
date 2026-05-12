import { UNSAFE_Combobox } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { getKodeverkStonadsTypeOptions } from "~/common/sak/sak.query";
import type { StonadType, TemaType } from "~/common/sak/sak.types";

interface Props {
    label: string;
    value?: StonadType;
    error?: string;
    onChange: (type: StonadType | undefined) => void;
    name?: string;
    readOnly?: boolean;
    temaFilter: Array<TemaType>;
}

export function StonadsTypeVelger({
    label,
    value,
    temaFilter,
    error,
    onChange,
    name = "stonadstype",
    readOnly,
}: Props) {
    const { data: alleStonadstyper } = useSuspenseQuery(getKodeverkStonadsTypeOptions());
    const selectValue: string = (value ?? "") as string;

    const filteredStonadstyper = alleStonadstyper
        .filter((s) => temaFilter.includes(s.tema))
        .sort((a, b) => a.navn.localeCompare(b.navn, "no"));

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
            shouldAutocomplete={true}
            onToggleSelected={(option) => changeStonadsType(option)}
            options={filteredStonadstyper.map((bt) => ({ label: bt.navn, value: bt.type }))}
            selectedOptions={
                alleStonadstyper
                    ?.filter((s) => s.type === selectValue)
                    .map((h) => ({ label: h.navn, value: h.type })) ?? []
            }
        />
    );
}
