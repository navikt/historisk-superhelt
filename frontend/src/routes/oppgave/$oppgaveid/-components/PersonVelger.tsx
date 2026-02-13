import {PersonIcon} from '@navikt/aksel-icons'
import {BodyShort, Button, HStack, Label, Loader, TextField, VStack} from '@navikt/ds-react'
import {useState} from 'react'
import {findPersonByFnr as findPerson} from "@generated"

export type PersonValue = {
    fnr: string
    navn?: string
}

interface Props {
    name: string
    label: string
    value: PersonValue
    error?: string
    onChange?: (value: PersonValue) => void
    readOnly?: boolean
}

export function PersonVelger(props: Props) {
    const [isSearching, setIsSearching] = useState(false)
    const [editMode, setEditmode] = useState(!props.value?.navn)
    const [searchFnr, setSearchFnr] = useState(props.value.fnr)
    const [searchError, setSearchError] = useState<string>()


    const handleSearch = async () => {
        if (!searchFnr) return

        setIsSearching(true)
        const {data, error} = await findPerson({body: {fnr: searchFnr}})
        setIsSearching(false)

        setSearchError(error?.detail)
        if (data) {
            props.onChange?.({fnr: searchFnr, navn: data.navn})
        }
        setEditmode(!!error)
    }

    const toggleEditmode = () => {
        setEditmode(true)
    }

    if (isSearching) {
        return <VStack gap="2"><Label>{props.label}</Label><Loader height="3rem"/></VStack>
    }

    if (!editMode) {
        return (
            <VStack gap="2">
                <input type="hidden" name={props.name} value={props.value.fnr}/>
                <Label>{props.label}</Label>
                <HStack height="3rem" gap="4" justify="space-between" align="center">
                    <HStack gap="2" align="center">
                        <PersonIcon title="person" fontSize="1.5rem"/>
                        <BodyShort>{props.value.navn}/{props.value.fnr}</BodyShort>
                    </HStack>
                    <Button variant="secondary" type="button" size="small" onClick={toggleEditmode} disabled={props.readOnly}>
                        Endre
                    </Button>
                </HStack>
            </VStack>
        )
    }

    return (
        <VStack gap="2">
            <HStack gap="4" align="end">
                <TextField
                    label={props.label}
                    value={searchFnr}
                    onChange={(e) => setSearchFnr(e.currentTarget.value)}
                    error={props.error || searchError}
                    maxLength={11}
                    onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                            e.preventDefault()
                            handleSearch()
                        }
                    }}
                />
                <Button variant="primary" type="button" size="small" onClick={handleSearch}>
                    Velg
                </Button>
            </HStack>
        </VStack>
    )
}
