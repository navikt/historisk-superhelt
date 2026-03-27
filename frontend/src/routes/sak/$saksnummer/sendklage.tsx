import type { HjemmelDto } from "@generated";
import { getKodeverkHjemlerOptions, sendKlageTilKabalMutation } from "@generated/@tanstack/react-query.gen";
import {
    BodyShort,
    Box,
    Button,
    DatePicker,
    Heading,
    HStack,
    Select,
    Tag,
    Textarea,
    useDatepicker,
    VStack,
} from "@navikt/ds-react";
import { useMutation, useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { dateTilIsoDato } from "~/common/dato.utils";
import { ErrorAlert } from "~/common/error/ErrorAlert";
import { useStonadsTypeNavn } from "~/common/sak/useStonadsTypeNavn";
import { getSakOptions } from "~/routes/sak/$saksnummer/-api/sak.query";
import { useInvalidateSakQuery } from "~/routes/sak/$saksnummer/-api/useInvalidateSakQuery";

export const Route = createFileRoute("/sak/$saksnummer/sendklage")({
    component: SendKlagePage,
});

const MAX_KOMMENTAR_LENGDE = 2000;

function SendKlagePage() {
    const { saksnummer } = Route.useParams();
    const { data: sak } = useSuspenseQuery(getSakOptions(saksnummer));
    const { data: hjemler } = useQuery({ ...getKodeverkHjemlerOptions(), staleTime: Number.POSITIVE_INFINITY });
    const invalidateSakQuery = useInvalidateSakQuery();
    const navigate = useNavigate();
    const getStonadsTypeNavn = useStonadsTypeNavn();

    const [valgtHjemmelId, setValgtHjemmelId] = useState<string>("");
    const [kommentar, setKommentar] = useState<string>("");
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
            navigate({
                to: "/sak/$saksnummer/oppsummering",
                params: { saksnummer },
            });
        },
    });

    const valgtHjemmel: HjemmelDto | undefined = hjemler?.find((h) => h.id === valgtHjemmelId);

    const validate = () => {
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

    const onSendKlage = async () => {
        if (!validate()) return;
        await sendKlage.mutateAsync({
            path: { saksnummer },
            body: {
                hjemmelId: valgtHjemmelId,
                datoKlageMottatt: dateTilIsoDato(valgtDato)!,
                kommentar: kommentar.trim() || undefined,
            },
        });
    };

    const onAngre = () => {
        navigate({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer } });
    };

    return (
        <VStack gap="space-32">
            <Heading size="medium">Send klage til Kabal</Heading>

            {/* Sak-info */}
            <Box background="neutral-moderate" borderRadius="2" padding="space-16">
                <VStack gap="space-8">
                    <Heading size="xsmall">Sak</Heading>
                    <HStack gap="space-8" align="center">
                        <Tag variant="neutral" size="small">
                            {sak.saksnummer}
                        </Tag>
                        <BodyShort>{getStonadsTypeNavn(sak.type)}</BodyShort>
                    </HStack>
                </VStack>
            </Box>

            {/* Dato klage mottatt */}
            <DatePicker {...datepickerProps}>
                <DatePicker.Input {...inputProps} label="Dato klage mottatt" error={datoError} />
            </DatePicker>

            {/* Hjemmel */}
            <VStack gap="space-8">
                <Select
                    label="Hjemmel"
                    description="Velg den lovhjemmelen klagen gjelder"
                    value={valgtHjemmelId}
                    onChange={(e) => {
                        setValgtHjemmelId(e.target.value);
                        setHjemmelError(undefined);
                    }}
                    error={hjemmelError}
                >
                    <option value="">– Velg hjemmel –</option>
                    {hjemler?.map((hjemmel) => (
                        <option key={hjemmel.id} value={hjemmel.id}>
                            {hjemmel.visningsnavn}
                        </option>
                    ))}
                </Select>

                {valgtHjemmel && (
                    <Box
                        background="default"
                        borderRadius="2"
                        borderColor="neutral-subtle"
                        borderWidth="1"
                        padding="space-12"
                    >
                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Valgt hjemmel</BodyShort>
                            <BodyShort weight="semibold">
                                {valgtHjemmel.lovKildeNavn} – {valgtHjemmel.spesifikasjon}
                            </BodyShort>
                            <Tag variant="neutral" size="small">
                                {valgtHjemmel.id}
                            </Tag>
                        </VStack>
                    </Box>
                )}
            </VStack>

            {/* Kommentar */}
            <Textarea
                label="Kommentar (valgfri)"
                description="Eventuell tilleggsinformasjon til Kabal"
                value={kommentar}
                onChange={(e) => setKommentar(e.target.value)}
                maxLength={MAX_KOMMENTAR_LENGDE}
                rows={4}
            />

            <ErrorAlert error={sendKlage.error} />

            <HStack gap="space-8" justify="end">
                <Button type="button" variant="tertiary" onClick={onAngre}>
                    Angre
                </Button>
                <Button
                    type="button"
                    variant="primary"
                    onClick={onSendKlage}
                    loading={sendKlage.isPending}
                    disabled={!sak.rettigheter.includes("SEND_KLAGE")}
                >
                    Send til Kabal
                </Button>
            </HStack>
        </VStack>
    );
}
