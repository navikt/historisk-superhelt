import {createFileRoute, Link, useNavigate} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Select, Textarea, TextField, VStack} from '@navikt/ds-react'
import {useState} from "react";
import {SakCreateRequestDto} from "@api";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkSakTypeOptions, getSakOptions} from "./-api/sak.query";
import {oppdaterSakMutation} from "@api/@tanstack/react-query.gen";


export const Route = createFileRoute('/sak/$saksnummer/soknad')({
    component: EditSakPage,
})


function EditSakPage() {
    const {saksnummer} = Route.useParams()
    const {data, isPending} = useSuspenseQuery(getSakOptions(saksnummer))
    const {data: saksTyper} = useSuspenseQuery(getKodeverkSakTypeOptions())
    const navigate = useNavigate()
    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
    })

    const [type, setType] = useState(data?.type)
    const [tittel, setTittel] = useState(data?.tittel)
    const [begrunnelse, setBegrunnelse] = useState(data?.begrunnelse)


    function handleSubmit() {
        oppdaterSak.mutate({
            path: {
                saksnummer: saksnummer
            },
            body: {
                type,
                tittel,
                begrunnelse
            }
        })
    }

    const error = oppdaterSak?.error?.detail
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
                            <option key={st.type} value={st.type}>
                                {st.navn}
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
                        <Button type="submit" loading={oppdaterSak?.status === 'pending'}>
                            Oppdater sak
                        </Button>
                        <Button as={Link}
                                type="button"
                                variant="secondary"
                                to={`/person/${data?.maskertPersonIdent}`}
                        >
                            Avbryt
                        </Button>
                    </HStack>
                </VStack>
            </form>
        </Box>
    )
}
