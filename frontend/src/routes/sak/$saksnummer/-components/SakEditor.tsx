import {
    Box,
    Button,
    DatePicker,
    ErrorSummary,
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
import {NumericInput} from "~/components/NumericInput";


interface Props {
    sak: Sak,
}

export default function SakEditor({sak}: Props) {
    const {data: saksTyper} = useSuspenseQuery(getKodeverkStonadsTypeOptions())
    const queryClient = useQueryClient();
    const navigate = useNavigate();
    const [showValidation, setShowValidation] = useState(false)
    const [hasChanged, setHasChanged] = useState(false)

    const saksnummer = sak.saksnummer

    const validationErrors = sak.valideringsfeil || []
    const hasValidationErrors = validationErrors.length > 0


    const oppdaterSak = useMutation({
        ...oppdaterSakMutation()
        , onSuccess: (data) => {
            queryClient.setQueryData(sakQueryKey(saksnummer), data)
        }
    })

    const [updateSakData, setUpdateSakData] = useState<SakUpdateRequestDto>({
        type: sak.type,
        tildelingsAar: sak.tildelingsAar,
        beskrivelse: sak.beskrivelse,
        vedtaksResultat: sak.vedtaksResultat,
        soknadsDato: sak.soknadsDato,
        begrunnelse: sak.begrunnelse
    })
    const debouncedSak = useDebounce(updateSakData, 2000)


    const {datepickerProps, inputProps} = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => patchSak({soknadsDato: dateTilIsoDato(day)}),
        defaultSelected: sak.soknadsDato ? new Date(sak.soknadsDato) : new Date()
    });

    useEffect(() => {
        // Lagrer etter siste endring
        if (debouncedSak && hasChanged) {
            lagreSak()
        }
    }, [debouncedSak]);


    const patchSak = (s: Partial<SakUpdateRequestDto>) => {
        setUpdateSakData(prev => ({...prev, ...s}))
        setHasChanged(true)
    }

    function lagreSak() {
        if (!hasChanged) {
            return Promise.resolve(sak)
        }
        const response = oppdaterSak.mutateAsync({
            path: {
                saksnummer: saksnummer
            },
            body: updateSakData
        });
        setHasChanged(false)
        return response
    }

    async function completedSoknad() {
        const lagretSak = await lagreSak()
        setShowValidation(true)
        if (lagretSak.valideringsfeil.length === 0) {
            navigate({to: "/sak/$saksnummer/vedtaksbrevbruker", params: {saksnummer}})
        }
    }

    const hasError: boolean = showValidation && (!!oppdaterSak?.error || hasValidationErrors)


    function getErrorMessage(field: "beskrivelse" | "vedtaksResultat" | "soknadsDato" | "begrunnelse" | "utbetaling.belop" | "utbetaling" | "tildelingsAar"): string | undefined {
        if (!showValidation || !hasValidationErrors) {
            return undefined
        }
        return validationErrors.find(feil => feil.field === field)?.message || undefined

    }

    return (
        <Box.New background={"neutral-soft"} padding="6" borderWidth="1" borderRadius="medium">

            <VStack gap="6">
                <Select
                    label="Stønad"
                    value={updateSakData.type}
                    onChange={(e) => patchSak({type: e.target.value as StonadType})}
                >
                    {saksTyper.map((st) => (
                        <option key={st.type} value={st.type}>
                            {st.navn}
                        </option>
                    ))}
                </Select>

                <HStack gap="6">
                    <DatePicker {...datepickerProps} >
                        <DatePicker.Input {...inputProps}
                                          label="Søknadsdato"
                                          error={getErrorMessage("soknadsDato")}
                        />
                    </DatePicker>
                    <NumericInput
                        label="Tildelingsår"
                        autoComplete="on"
                        error={getErrorMessage("tildelingsAar")}
                        value={updateSakData.tildelingsAar}
                        onChange={(value) => patchSak({tildelingsAar: value})}
                    />
                </HStack>

                <TextField
                    label="Kort beskrivelse av stønad"
                    error={getErrorMessage("beskrivelse")}
                    value={updateSakData.beskrivelse ?? ''}
                    onChange={(e) => patchSak({beskrivelse: e.target.value})}
                />


                <Box.New background={"default"} padding={"space-16"}>
                    <VStack gap="space-16">
                        <RadioGroup legend="Vedtak" value={updateSakData.vedtaksResultat}
                                    onChange={value => patchSak({vedtaksResultat: value as SakVedtakType})}
                                    error={getErrorMessage("vedtaksResultat")}>
                            <Radio value="INNVILGET">Innvilget</Radio>
                            <Radio value="DELVIS_INNVILGET">Delvis innvilget</Radio>
                            <Radio value="AVSLATT">Avslått</Radio>
                        </RadioGroup>

                        {["INNVILGET", "DELVIS_INNVILGET"].includes(updateSakData.vedtaksResultat ?? "") && (
                            <UtbetalingEditor sak={sak}
                                              errorUtbetaling={getErrorMessage("utbetaling")}
                                              errorBelop={getErrorMessage("utbetaling.belop")}
                            />
                        )}

                        <Textarea
                            label="Begrunnelse for vedtak"
                            error={getErrorMessage("begrunnelse")}
                            value={updateSakData.begrunnelse ?? ''}
                            onChange={(e) => patchSak({begrunnelse: e.target.value})}
                            description="Beskriv kort hva som ligger til grunn for vedtaket"
                            minRows={4}
                        />
                    </VStack>
                </Box.New>


                <HStack gap="8" align="start">
                    <Button type="submit" variant="secondary" onClick={completedSoknad}>Lagre og gå videre</Button>
                </HStack>
                {hasError && <ErrorSummary>
                    {oppdaterSak.error && <ErrorSummary.Item>{oppdaterSak?.error?.detail}</ErrorSummary.Item>}
                    {validationErrors.map((feil) => (
                        <ErrorSummary.Item key={feil.field}>{feil.message}</ErrorSummary.Item>
                    ))}

                </ErrorSummary>}

            </VStack>

        </Box.New>
    )
}
