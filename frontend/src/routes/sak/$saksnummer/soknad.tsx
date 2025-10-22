import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Select, Textarea, TextField, VStack} from '@navikt/ds-react'
import {useState} from "react";
import {SakCreateRequestDto} from "@api";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getSakOptions} from "./-api/sak.query";


export const Route = createFileRoute('/sak/$saksnummer/soknad')({
    component: EditSakPage,
})

const saksTyper = [
    {value: 'PARYKK', label: 'Parykk'},
    {value: 'ORTOSE', label: 'Ortose'},
    {value: 'PROTESE', label: 'Protese'},
    {value: 'FOTTOY', label: 'Fottøy'},
    {value: 'REISEUTGIFTER', label: 'Reiseutgifter'},
] as const

function EditSakPage() {
    const {saksnummer} = Route.useParams()
    const {data, isPending} = useSuspenseQuery(getSakOptions(saksnummer))
    const navigate = useNavigate()

    const [type, setType] = useState(data?.type)
    const [tittel, setTittel] = useState(data?.tittel)
    const [begrunnelse, setBegrunnelse] = useState(data?.begrunnelse)
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState<string>()


    function handleSubmit() {

    }

    function handleCancel() {

    }

    return (

            <Box padding="6" borderWidth="1" borderRadius="medium">
                <form onSubmit={handleSubmit}>
                    <VStack gap="6">
                        <Heading size="medium">Saksdetaljer</Heading>

                        <Select
                            label="Sakstype"
                            value={type}
                            onChange={(e) => setType(e.target.value as SakCreateRequestDto['type'])}
                        >
                            {saksTyper.map((st) => (
                                <option key={st.value} value={st.value}>
                                    {st.label}
                                </option>
                            ))}
                        </Select>

                        <TextField
                            label="Tittel"
                            value={tittel}
                            onChange={(e) => setTittel(e.target.value)}
                            description="Valgfri - kort beskrivelse av saken"
                        />

                        <Textarea
                            label="Begrunnelse"
                            value={begrunnelse}
                            onChange={(e) => setBegrunnelse(e.target.value)}
                            description="Valgfri - utfyllende begrunnelse for saken"
                            minRows={4}
                        />

                        {error && (
                            <Box padding="4" borderRadius="medium"
                                 style={{backgroundColor: 'var(--a-surface-danger-subtle)'}}>
                                {error}
                            </Box>
                        )}

                        <HStack gap="4">
                            <Button type="submit" loading={isSubmitting}>
                                Oppdater sak
                            </Button>
                            <Button
                                type="button"
                                variant="secondary"
                                onClick={handleCancel}
                                disabled={isSubmitting}
                            >
                                Avbryt
                            </Button>
                        </HStack>
                    </VStack>
                </form>
            </Box>
    )
}
