import {TextField, VStack} from '@navikt/ds-react'

interface Props {
    value?: string
    index?: number
    name?: string
    error?: string
}

function getDokumentLabel(index: number): string {
    return index > 0 ? `Dokumenttittel ${index + 1}` : 'Dokumenttittel'
}

export function DokumentTittelFelt({value, error, name, index = 0}: Props) {
    return (
        <VStack gap="space-16">
            <TextField
                label={getDokumentLabel(index)}
                name={name}
                error={error}
                defaultValue={value}
            />
        </VStack>
    )
}
