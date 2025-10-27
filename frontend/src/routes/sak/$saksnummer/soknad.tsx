import {createFileRoute, Link} from '@tanstack/react-router'
import {Box, Button, Heading, HStack, Radio, RadioGroup, Select, Textarea, TextField, VStack} from '@navikt/ds-react'
import {useState} from "react";
import {SakCreateRequestDto, SakDto} from "@api";
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
    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
    })

    const [payoutType, setPayoutType] = useState<'bruker' | 'faktura'| 'ingen'>('ingen')
    const [payoutAmount, setPayoutAmount] = useState('')

    const [sak, setSak] = useState<SakDto>(data)


    const patchSak = (s: Partial<SakDto>) => {
        setSak(prev => ({...prev, ...s}))
    }

    function handleSubmit() {
        oppdaterSak.mutate({
            path: {
                saksnummer: saksnummer
            },
            body: sak
        })
    }

    const error = oppdaterSak?.error?.detail
    return (
        <form onSubmit={(e) => {
            e.preventDefault();
            handleSubmit();
        }}>
            <Box padding="6" borderWidth="1" borderRadius="medium">

                    <VStack gap="6">

                        <Select
                            label="Sakstype"
                            value={sak.type}
                            onChange={(e) => patchSak({type: e.target.value as SakCreateRequestDto['type']})}
                        >
                            {saksTyper.map((st) => (
                                <option key={st.type} value={st.type}>
                                    {st.navn}
                                </option>
                            ))}
                        </Select>

                        <TextField
                            label="Tittel"
                            value={sak.tittel}
                            onChange={(e) => patchSak({tittel: e.target.value})}
                            description="Valgfri - kort beskrivelse av saken"
                        />

                        <Textarea
                            label="Begrunnelse"
                            value={sak.begrunnelse}
                            onChange={(e) => patchSak({begrunnelse: e.target.value})}
                            description="Valgfri - utfyllende begrunnelse for saken"
                            minRows={4}
                        />




                    <Heading size="small">Fatte vedtak</Heading>

                    <HStack gap="8" align="start">
                        <VStack style={{flex: 1}}>
                            <RadioGroup legend="Vedtak" value={sak.status} onChange={value=>patchSak({status:value as SakDto['status']})}>
                                <Radio value="INNVILGET">Innvilget</Radio>
                                <Radio value="DELVIS_INNVILGET">Delvis innvilget</Radio>
                                <Radio value="AVSLATT">Avslått</Radio>
                            </RadioGroup>
                        </VStack>
                        {sak.status !== 'AVSLATT' && (
                            <VStack style={{flex: 1}}>
                                <RadioGroup
                                    legend="Utbetaling"
                                    value={payoutType}
                                    onChange={v => setPayoutType(v as 'bruker' | 'faktura')}
                                >
                                    <Radio value="bruker">Utbetaling til bruker</Radio>
                                    {payoutType === 'bruker' && (
                                        <div style={{marginLeft: 32, marginTop: 4}}>
                                            <input
                                                type="number"
                                                min="0"
                                                step="1"
                                                placeholder="Beløp (kr)"
                                                value={payoutAmount}
                                                onChange={e => setPayoutAmount(e.target.value)}
                                                style={{width: 120}}
                                            />
                                        </div>
                                    )}
                                    <Radio value="faktura">Venter faktura</Radio>
                                </RadioGroup>
                            </VStack>
                        )}
                    </HStack>

                    {error && (
                        <Box padding="4" borderRadius="medium"
                             style={{backgroundColor: 'var(--a-surface-danger-subtle)'}}>
                            {error}
                        </Box>
                    )}

                    <HStack gap="4">
                        <Button
                            as={Link}
                            to="/sak/$saksnummer/brev"
                            variant="primary"
                            disabled={sak.status== "UNDER_BEHANDLING" }
                        >
                            Fatte vedtak
                        </Button>

                            <Button type="submit" loading={oppdaterSak?.status === 'pending'} variant="secondary">
                                Lagre kladd
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
            </Box>
        </form>
    )
}
