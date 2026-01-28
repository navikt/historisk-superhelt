import {Select} from '@navikt/ds-react'
import {StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions} from "~/routes/sak/$saksnummer/-api/sak.query";


interface Props {
    value?: StonadType
    error?: string
    onChange: (type: StonadType | undefined) => void
    name: string
}

export function StonadsTypeVelger({value, error, onChange, name}: Props) {
    const {data: stonadsTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const selectValue: string = (value ?? '') as string

    return (
        <Select
            label="Velg type søknad"
            name={name}
            value={selectValue}
            error={error}
            onChange={(e) => {
                const v = e.target.value
                onChange(v === '' ? undefined : (v as StonadType))
            }}
        >
            <option value={''}>-Velg type -</option>
            {stonadsTyper.map((bt) => (
                <option key={bt.type} value={bt.type}>
                    {bt.navn}
                </option>
            ))}
        </Select>
    )
}
