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
import {useEffect, useState} from "react";
import {Sak, SakUpdateRequestDto} from "@api";
import {useMutation, useQueryClient, useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions, sakQueryKey} from "../-api/sak.query";
import {ferdigstillSakMutation, oppdaterSakMutation} from "@api/@tanstack/react-query.gen";
import {dateTilIsoDato} from "~/components/dato.utils";
import {SakVedtakType, StonadType, UtbetalingsType} from "~/routes/sak/$saksnummer/-types/sak.types";
import {NumericInput} from "~/components/NumericInput";
import useDebounce from "~/components/useDebounce";


interface Props {
    sak: Sak,
}

export default function SakEditor({sak}: Props) {
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const queryClient = useQueryClient();
    const saksnummer = sak.saksnummer

    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
    })
    const ferdigStillSak = useMutation({
        ...ferdigstillSakMutation()
        , onSettled: () => {
            queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    const [updateSakData, setUpdateSakData] = useState<SakUpdateRequestDto>({...sak})
    const debouncedSak = useDebounce(updateSakData, 2000)


    const {datepickerProps, inputProps, selectedDay} = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => patchSak({soknadsDato: dateTilIsoDato(day)}),
        defaultSelected: sak.soknadsDato ? new Date(sak.soknadsDato) : new Date()
    });

    useEffect(() => {
        // Lagrer etter siste endring
        if (debouncedSak) {
            lagreSak()
        }
    }, [debouncedSak]); // Only re-run when debouncedSearchTerm changes


    const patchSak = (s: Partial<SakUpdateRequestDto>) => {
        setUpdateSakData(prev => ({...prev, ...s}))
    }

    function lagreSak() {
        oppdaterSak.mutate({
            path: {
                saksnummer: saksnummer
            },
            body: updateSakData
        })
    }

    const error = oppdaterSak?.error?.detail
    const changeUtbetalingsType = (v: UtbetalingsType) => {
        patchSak({utbetalingsType: v});
        if (v === 'FORHANDSTILSAGN') {
            patchSak({utbetaling: undefined})
            patchSak({forhandstilsagn: {dummy: true}})
        }
        if (v === 'BRUKER') {
            patchSak({forhandstilsagn: undefined})
        }

    }

    async function fatteVedtak() {
        await oppdaterSak.mutateAsync({
            path: {saksnummer: saksnummer}
            , body: updateSakData
        })
        //TODO Validering
        ferdigStillSak.mutate({
                path: {
                    saksnummer: saksnummer
                }
            }
        )
    }

    return (
        <form onSubmit={(e) => {
            e.preventDefault();
            lagreSak();
        }}>
            <Box padding="6" borderWidth="1" borderRadius="medium">

                <VStack gap="6">
                    <Select
                        label="Stønadstype"
                        value={updateSakData.type}
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
                        value={updateSakData.tittel}
                        onChange={(e) => patchSak({tittel: e.target.value})}
                    />


                    <Heading size="small">Fatte vedtak</Heading>

                    <HStack gap="8" align="start">
                        <VStack style={{flex: 1}}>
                            <RadioGroup legend="Vedtak" value={updateSakData.vedtak}
                                        onChange={value => patchSak({vedtak: value as SakVedtakType})}>
                                <Radio value="INNVILGET">Innvilget</Radio>
                                <Radio value="DELVIS_INNVILGET">Delvis innvilget</Radio>
                                <Radio value="AVSLATT">Avslått</Radio>
                            </RadioGroup>
                        </VStack>
                        {updateSakData.vedtak !== 'AVSLATT' && (
                            <VStack style={{flex: 1}}>
                                <RadioGroup
                                    legend="Utbetaling"
                                    value={updateSakData.utbetalingsType}
                                    onChange={changeUtbetalingsType}
                                >
                                    <Radio value="BRUKER">Utbetaling til bruker</Radio>
                                    {updateSakData.utbetalingsType === 'BRUKER' && (
                                        <NumericInput
                                            value={updateSakData.utbetaling?.belop}
                                            onChange={belop => patchSak({utbetaling: {belop: belop ?? 0}})}
                                            label="Beløp (kr)"/>
                                    )}
                                    <Radio value="FORHANDSTILSAGN">Forhåndstilsagn (faktura kommer)</Radio>

                                </RadioGroup>
                            </VStack>
                        )}
                    </HStack>

                    <Textarea
                        label="Saksbehandlers vurderinger"
                        value={updateSakData.begrunnelse ?? ''}
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
                            variant="primary"
                            disabled={!updateSakData.vedtak}
                            onClick={fatteVedtak}
                            loading={ferdigStillSak?.status === 'pending' || oppdaterSak?.status === 'pending'}
                        >
                            Fatte vedtak
                        </Button>

                    </HStack>


                </VStack>
            </Box>
        </form>
    )
}
