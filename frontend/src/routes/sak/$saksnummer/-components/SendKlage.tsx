import {
    getKodeverkHjemlerForStonadOptions,
    getUserInfoOptions,
    sendKlageTilKabalMutation,
} from "@generated/@tanstack/react-query.gen";
import {
    Button,
    DatePicker,
    Dialog,
    LocalAlert,
    Select,
    Textarea,
    UNSAFE_Combobox,
    useDatepicker,
    VStack,
} from "@navikt/ds-react";
import { BreakpointMd } from "@navikt/ds-tokens/dist/tokens";
import { useMutation, useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { useParams } from "@tanstack/react-router";
import { useState } from "react";
import { dateTilIsoDato } from "~/common/dato.utils";
import { getSakOptions } from "~/common/sak/sak.query";
import { useInvalidateSakQuery } from "../-api/useInvalidateSakQuery";

const MAX_KOMMENTAR_LENGDE = 2000;

interface SendKlageProps {
    readonly open: boolean;
    readonly onOpenChange: (open: boolean) => void;
}

export function SendKlage({ open, onOpenChange }: SendKlageProps) {
    const { saksnummer } = useParams({ from: "/sak/$saksnummer" });
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    // Henter kun hjemler som er gyldige for saken
    const { data: hjemler } = useQuery({
        ...getKodeverkHjemlerForStonadOptions({
            path: { stonadsType: sak.type },
        }),
        staleTime: Number.POSITIVE_INFINITY,
        enabled: open,
    });
    const { data: navAnsatt } = useSuspenseQuery(getUserInfoOptions());
    const enheter = navAnsatt.enheter;
    const intitialEnhet = enheter[0]?.enhetnummer ?? "";

    const invalidateSakQuery = useInvalidateSakQuery();
    const [valgtHjemmelId, setValgtHjemmelId] = useState<string>("");
    const [kommentar, setKommentar] = useState<string>("");
    const [enhet, setEnhet] = useState<string>(intitialEnhet);
    const [hjemmelError, setHjemmelError] = useState<string | undefined>();
    const [datoError, setDatoError] = useState<string | undefined>();
    const [valgtDato, setValgtDato] = useState<Date | undefined>();

    const { datepickerProps, inputProps } = useDatepicker({
        toDate: new Date(),
        onDateChange: (day) => {
            setValgtDato(day);
            setDatoError(undefined);
        },
    });

    const sendKlage = useMutation({
        ...sendKlageTilKabalMutation(),
        onSuccess: () => {
            invalidateSakQuery(saksnummer);
        },
    });

    const resetState = () => {
        setValgtHjemmelId("");
        setKommentar("");
        setHjemmelError(undefined);
        setDatoError(undefined);
        setValgtDato(undefined);
        setEnhet(intitialEnhet);
        sendKlage.reset();
    };

    const handleOpenChange = (nextOpen: boolean) => {
        if (!nextOpen) resetState();
        onOpenChange(nextOpen);
    };

    const validerSkjema = () => {
        let valid = true;
        if (!valgtHjemmelId) {
            setHjemmelError("Du må velge en hjemmel");
            valid = false;
        } else {
            setHjemmelError(undefined);
        }
        if (!valgtDato) {
            setDatoError("Du må oppgi dato for når klagen ble mottatt");
            valid = false;
        } else {
            setDatoError(undefined);
        }
        return valid;
    };

    const handleBekreftOgSend = async () => {
        if (!validerSkjema()) return;

        await sendKlage.mutateAsync({
            path: { saksnummer },
            body: {
                hjemmelId: valgtHjemmelId,
                datoKlageMottatt: dateTilIsoDato(valgtDato) ?? "",
                enhet: enhet,
                kommentar: kommentar.trim() || undefined,
            },
        });
    };

    return (
        <Dialog open={open} onOpenChange={handleOpenChange}>
            <Dialog.Popup closeOnOutsideClick={false} style={{ width: BreakpointMd, resize: "both", overflow: "auto" }}>
                <Dialog.Header>
                    <Dialog.Title>Send klage til Kabal</Dialog.Title>
                </Dialog.Header>

                <Dialog.Body style={{ height: "100%" }}>
                    <VStack gap="space-16">
                        {sendKlage.isSuccess && (
                            <LocalAlert status="success">
                                <LocalAlert.Header>
                                    <LocalAlert.Title>Klage sendt</LocalAlert.Title>
                                </LocalAlert.Header>
                                <LocalAlert.Content>Klagen ble oversendt til Kabal og er mottatt.</LocalAlert.Content>
                            </LocalAlert>
                        )}
                        {sendKlage.isError && (
                            <LocalAlert status="error">
                                <LocalAlert.Header>
                                    <LocalAlert.Title>Sending til Kabal feilet</LocalAlert.Title>
                                </LocalAlert.Header>
                                <LocalAlert.Content>
                                    {sendKlage.error?.detail ??
                                        "En ukjent feil oppstod. Prøv igjen eller kontakt support."}
                                </LocalAlert.Content>
                            </LocalAlert>
                        )}
                        <DatePicker {...datepickerProps}>
                            <DatePicker.Input {...inputProps} label="Dato klage mottatt" error={datoError} />
                        </DatePicker>

                        <UNSAFE_Combobox
                            label="Hjemmel"
                            description="Velg den lovhjemmelen klagen gjelder"
                            options={
                                hjemler?.map((hjemmel) => ({
                                    label: hjemmel.visningsnavn,
                                    value: hjemmel.id,
                                })) ?? []
                            }
                            selectedOptions={
                                hjemler
                                    ?.filter((h) => h.id === valgtHjemmelId)
                                    .map((h) => ({ label: h.visningsnavn, value: h.id })) ?? []
                            }
                            onToggleSelected={(option, isSelected) => {
                                setValgtHjemmelId(isSelected ? option : "");
                                setHjemmelError(undefined);
                            }}
                            shouldAutocomplete
                            error={hjemmelError}
                        />

                        <Select
                            label={"Enhet"}
                            description="Velg hvilken geografisk enhet du jobber i"
                            value={enhet}
                            onChange={(event) => setEnhet(event.target.value)}
                        >
                            {enheter.map((e) => (
                                <option key={e.enhetnummer} value={e.enhetnummer}>
                                    {e.navn}
                                </option>
                            ))}
                        </Select>

                        <Textarea
                            label="Kommentar (valgfri)"
                            description="Eventuell tilleggsinformasjon til Kabal"
                            value={kommentar}
                            onChange={(e) => setKommentar(e.target.value)}
                            maxLength={MAX_KOMMENTAR_LENGDE}
                            rows={4}
                        />
                    </VStack>
                </Dialog.Body>
                <Dialog.Footer>
                    {!sendKlage.isSuccess && (
                        <Button
                            type="button"
                            variant="primary"
                            onClick={handleBekreftOgSend}
                            loading={sendKlage.isPending}
                            disabled={!sak.rettigheter.includes("SEND_KLAGE")}
                        >
                            Send klage til Kabal
                        </Button>
                    )}
                    <Button type="button" variant="secondary" onClick={() => handleOpenChange(false)}>
                        {sendKlage.isSuccess ? "Lukk" : "Avbryt"}
                    </Button>
                </Dialog.Footer>
            </Dialog.Popup>
        </Dialog>
    );
}
