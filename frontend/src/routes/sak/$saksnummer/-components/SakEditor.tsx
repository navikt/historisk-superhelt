import {
    Box,
    Button,
    DatePicker,
    ErrorSummary,
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
import {Sak, SakUpdateRequestDto} from "@generated";
import {useMutation, useQueryClient, useSuspenseQuery} from "@tanstack/react-query";
import {getKodeverkStonadsTypeOptions, sakQueryKey} from "../-api/sak.query";
import {oppdaterSakMutation} from "@generated/@tanstack/react-query.gen";
import {dateTilIsoDato} from "~/components/dato.utils";
import {SakVedtakType, StonadType} from "~/routes/sak/$saksnummer/-types/sak.types";
import useDebounce from "~/components/useDebounce";
import UtbetalingEditor from "~/routes/sak/$saksnummer/-components/UtbetalingEditor";
import {useNavigate} from "@tanstack/react-router";


interface Props {
    sak: Sak,
}

export default function SakEditor({sak}: Props) {
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const queryClient = useQueryClient();
    const navigate = useNavigate();

    const saksnummer = sak.saksnummer

    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
        , onSuccess: () => {
            queryClient.invalidateQueries({queryKey: sakQueryKey(saksnummer)})
        }
    })

    const [updateSakData, setUpdateSakData] = useState<SakUpdateRequestDto>({...sak})
    const debouncedSak = useDebounce(updateSakData, 2000)


    const {datepickerProps, inputProps} = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => patchSak({soknadsDato: dateTilIsoDato(day)}),
        defaultSelected: sak.soknadsDato ? new Date(sak.soknadsDato) : new Date()
    });

    useEffect(() => {
        // Lagrer etter siste endring
        if (debouncedSak) {
            lagreSak()
        }
    }, [debouncedSak]);


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

    function completedSoknad() {
        lagreSak()
        // TODO Validate
        navigate({to: "/sak/$saksnummer/brev", params: {saksnummer}})
    }

    const error = oppdaterSak?.error


    return (
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
                    value={updateSakData.tittel ?? ''}
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
                        <UtbetalingEditor sak={sak}/>
                    )}
                </HStack>

                <Textarea
                    label="Saksbehandlers vurderinger"
                    value={updateSakData.begrunnelse ?? ''}
                    onChange={(e) => patchSak({begrunnelse: e.target.value})}
                    description="Valgfri - vurderinger som er gjort i saken. Kommer ikke med i vedtaksbrev."
                    minRows={4}
                />


                {error && <ErrorSummary>
                    <ErrorSummary.Item>{error?.detail}</ErrorSummary.Item>

                </ErrorSummary>}
                <HStack gap="8" align="start">
                    <Button type="submit" variant="secondary" onClick={completedSoknad}>Gå til brev</Button>
                </HStack>

            </VStack>

        </Box>
    )
}
