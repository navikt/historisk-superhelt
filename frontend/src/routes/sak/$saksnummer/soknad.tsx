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
import {SakUpdateRequestDto} from "@api";
import {useMutation, useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions, getSakOptions} from "./-api/sak.query";
import {oppdaterSakMutation} from "@api/@tanstack/react-query.gen";
import {dateTilIsoDato} from "~/components/dato.utils";
import {SakVedtakType, StonadType, UtbetalingsType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {NumericInput} from "~/components/NumericInput";


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

    const [sak, setSak] = useState<SakUpdateRequestDto>({...data})

    const {datepickerProps, inputProps, selectedDay} = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => patchSak({soknadsDato: dateTilIsoDato(day)}),
        defaultSelected: data.soknadsDato ? new Date(data.soknadsDato) : new Date()
    });

    const patchSak = (s: Partial<SakUpdateRequestDto>) => {
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
                                    value={sak.utbetalingsType}
                                    onChange={v => patchSak({utbetalingsType: v as UtbetalingsType})}
                                >
                                    <Radio value="BRUKER">Utbetaling til bruker</Radio>
                                    {sak.utbetalingsType === 'BRUKER' && (
                                        <NumericInput
                                            value={sak.utbetaling?.belop}
                                            onChange={belop => patchSak({utbetaling: {belop: belop} })}
                                            label="Beløp (kr)"/>
                                    )}
                                    <Radio value="FORHANDSTILSAGN" onClick={()=> patchSak({forhandstilsagn:{dummy: true}})}>Forhåndstilsagn (faktura kommer)</Radio>

                                </RadioGroup>
                            </VStack>
                        )}
                    </HStack>

                    <Textarea
                        label="Saksbehandlers vurderinger"
                        value={sak.begrunnelse}
                        onChange={(e) => patchSak({begrunnelse: e.target.value})}
                        description="Valgfri - vurderinger som er gjort i saken. Kommer ikke med i vedtaksbrev."
                        minRows={4}
                    />


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



                </VStack>
            </Box>
        </form>
    )
}
