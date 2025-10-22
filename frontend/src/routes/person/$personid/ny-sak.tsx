import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Select, Textarea, TextField, VStack} from '@navikt/ds-react'
import {useState} from "react";
import {createSak, SakCreateRequestDto} from "@api";
import {PersonHeader} from "~/components/PersonHeader";
import {useQuery} from "@tanstack/react-query";
import {getPersonByMaskertIdentOptions} from "@api/@tanstack/react-query.gen";

export const Route = createFileRoute('/person/$personid/ny-sak')({
    component: NySakPage,
})

const saksTyper = [
    {value: 'PARYKK', label: 'Parykk'},
    {value: 'ORTOSE', label: 'Ortose'},
    {value: 'PROTESE', label: 'Protese'},
    {value: 'FOTTOY', label: 'Fottøy'},
    {value: 'REISEUTGIFTER', label: 'Reiseutgifter'},
] as const

function NySakPage() {
    const {personid} = Route.useParams()
    const navigate = useNavigate()

    const [type, setType] = useState<SakCreateRequestDto['type']>('PARYKK')
    const [tittel, setTittel] = useState('')
    const [begrunnelse, setBegrunnelse] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState<string>()

    const {data: person} = useQuery(
        {
            ...getPersonByMaskertIdentOptions({
                path: {
                    maskertPersonident: personid
                }
            })
        })

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault()
        setError(undefined)
        setIsSubmitting(true)

        try {
            const {data, error: apiError} = await createSak({
                body: {
                    type,
                    fnr: person?.fnr!,
                    tittel: tittel || undefined,
                    begrunnelse: begrunnelse || undefined,
                }
            })

            if (apiError) {
                setError('Kunne ikke opprette sak: ' + (apiError.detail || 'Ukjent feil'))
                setIsSubmitting(false)
                return
            }
            // Navigate back to person page after successful creation
            await navigate({to: '/person/$personid', params: {personid: personid}})

        } catch (err) {
            setError('Noe gikk galt ved opprettelse av sak')
            setIsSubmitting(false)
        }
    }

    function handleCancel() {
        navigate({to: '/person/$personid', params: {personid}})
    }

    return (
        <VStack gap="6">

            <Heading size="xlarge">Opprett ny sak</Heading>
            <PersonHeader maskertPersonId={personid}/>

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
                                Opprett sak
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
        </VStack>
    )
}
