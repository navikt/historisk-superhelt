import {createFileRoute, Link} from '@tanstack/react-router'
import {
    Box,
    Button,
    DatePicker,
    Heading,
    HStack,
    Radio,
    RadioGroup,
    Select,
    Textarea,
    TextField,
    useDatepicker,
    VStack
} from '@navikt/ds-react'
import {useState} from "react";
import { SakDto} from "@api";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions, getSakOptions} from "./-api/sak.query";
import {oppdaterSakMutation} from "@api/@tanstack/react-query.gen";
import {dateTilIsoDato} from "~/components/dato.utils";
import {SakVedtakType, StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";


export const Route = createFileRoute('/sak/$saksnummer/soknad')({
    component: EditSakPage,
})


function EditSakPage() {
    const {saksnummer} = Route.useParams()
    const {data, isPending} = useSuspenseQuery(getSakOptions(saksnummer))
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
    })

    const [payoutType, setPayoutType] = useState<'bruker' | 'faktura' | 'ingen'>('ingen')
    const [payoutAmount, setPayoutAmount] = useState('')

    const [sak, setSak] = useState<SakDto>(data)

    const {datepickerProps, inputProps, selectedDay} = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => patchSak({soknadsDato: dateTilIsoDato(day)}),
        defaultSelected: data.soknadsDato ? new Date(data.soknadsDato) : new Date()
    });

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
                        label="Stønadstype"
                        value={sak.type}
                        onChange={(e) => patchSak({type: e.target.value as StonadType})}
                    >
                        {saksTyper.map((st) => (
                            <option key={st.type} value={st.type}>
                                {st.navn}
                            </option>
                        ))}
                    </Select>

                    <DatePicker {...datepickerProps} >
                        <DatePicker.Input {...inputProps} label="Søknadsdato"/>
                    </DatePicker>

                    <TextField
                        label="Tittel"
                        value={sak.tittel}
                        onChange={(e) => patchSak({tittel: e.target.value})}
                    />


                    <Heading size="small">Fatte vedtak</Heading>

                    <HStack gap="8" align="start">
                        <VStack style={{flex: 1}}>
                            <RadioGroup legend="Vedtak" value={sak.vedtak}
                                        onChange={value => patchSak({vedtak: value as SakVedtakType})}>
                                <Radio value="INNVILGET">Innvilget</Radio>
                                <Radio value="DELVIS_INNVILGET">Delvis innvilget</Radio>
                                <Radio value="AVSLATT">Avslått</Radio>
                            </RadioGroup>
                        </VStack>
                        {sak.vedtak !== 'AVSLATT' && (
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
                                    <Radio value="faktura">Forhåndstilsagn (faktura kommer)</Radio>
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
                            disabled={!sak.vedtak}
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

                    <Textarea
                        label="Notat"
                        value={sak.begrunnelse}
                        onChange={(e) => patchSak({begrunnelse: e.target.value})}
                        description="Valgfri - vurderinger som er gjort i saken. Kommer ikke med i vedtaksbrev."
                        minRows={4}
                    />

                </VStack>
            </Box>
        </form>
    )
}
