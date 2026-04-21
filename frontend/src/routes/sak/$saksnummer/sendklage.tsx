import type { HjemmelDto } from "@generated";
import { getKodeverkHjemlerOptions, sendKlageTilKabalMutation } from "@generated/@tanstack/react-query.gen";
import {
    Alert,
    BodyLong,
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
import { dateTilIsoDato, isoTilLokal } from "~/common/dato.utils";
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
    const [visBekreftelse, setVisBekreftelse] = useState(false);
    const [klageSendt, setKlageSendt] = useState(false);

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
            setKlageSendt(true);
            invalidateSakQuery(saksnummer);
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

    /** Step 1 → 2: validate and show confirmation summary */
    const onVisBekreftelse = () => {
        if (!validate()) return;
        sendKlage.reset();
        setVisBekreftelse(true);
    };

    /** Step 2 → 3: actually POST to Kabal */
    const onBekreftOgSend = async () => {
        await sendKlage.mutateAsync({
            path: { saksnummer },
            body: {
                hjemmelId: valgtHjemmelId,
                datoKlageMottatt: dateTilIsoDato(valgtDato) ?? "",
                kommentar: kommentar.trim() || undefined,
            },
        });
    };

    const onAngre = () => {
        navigate({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer } });
    };

    // ── Step 3: Success ───────────────────────────────────────────────────────
    if (klageSendt) {
        return (
            <VStack gap="space-32">
                <Heading size="medium">Klage sendt til Kabal</Heading>

                <Alert variant="success">Klagen ble oversendt til Kabal og er mottatt.</Alert>

                <Box background="neutral-moderate" borderRadius="2" padding="space-20">
                    <VStack gap="space-12">
                        <Heading size="xsmall">Oppsummering</Heading>

                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Sak</BodyShort>
                            <HStack gap="space-8" align="center">
                                <Tag variant="neutral" size="small">
                                    {sak.saksnummer}
                                </Tag>
                                <BodyShort>{getStonadsTypeNavn(sak.type)}</BodyShort>
                            </HStack>
                        </VStack>

                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Hjemmel</BodyShort>
                            <HStack gap="space-8" align="center">
                                <BodyShort>
                                    {valgtHjemmel?.lovKildeNavn} – {valgtHjemmel?.spesifikasjon}
                                </BodyShort>
                                <Tag variant="neutral" size="small">
                                    {valgtHjemmel?.id}
                                </Tag>
                            </HStack>
                        </VStack>
                    </VStack>
                </Box>

                <HStack justify="end">
                    <Button
                        type="button"
                        variant="primary"
                        onClick={() => navigate({ to: "/sak/$saksnummer/oppsummering", params: { saksnummer } })}
                    >
                        Gå til oppsummering
                    </Button>
                </HStack>
            </VStack>
        );
    }

    // ── Step 2: Confirmation summary ──────────────────────────────────────────
    if (visBekreftelse) {
        return (
            <VStack gap="space-32">
                <Heading size="medium">Bekreft sending til Kabal</Heading>

                <Alert variant="info">
                    Kontroller at informasjonen nedenfor er korrekt før du sender klagen til Kabal.
                </Alert>

                <Box background="neutral-moderate" borderRadius="2" padding="space-20">
                    <VStack gap="space-16">
                        <Heading size="xsmall">Oppsummering</Heading>

                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Sak</BodyShort>
                            <HStack gap="space-8" align="center">
                                <Tag variant="neutral" size="small">
                                    {sak.saksnummer}
                                </Tag>
                                <BodyShort>{getStonadsTypeNavn(sak.type)}</BodyShort>
                            </HStack>
                        </VStack>

                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Dato klage mottatt</BodyShort>
                            <BodyShort>{isoTilLokal(valgtDato?.toISOString() ?? "")}</BodyShort>
                        </VStack>

                        <VStack gap="space-4">
                            <BodyShort weight="semibold">Hjemmel</BodyShort>
                            <HStack gap="space-8" align="center">
                                <BodyShort>
                                    {valgtHjemmel?.lovKildeNavn} – {valgtHjemmel?.spesifikasjon}
                                </BodyShort>
                                <Tag variant="neutral" size="small">
                                    {valgtHjemmel?.id}
                                </Tag>
                            </HStack>
                        </VStack>

                        {kommentar.trim() && (
                            <VStack gap="space-4">
                                <BodyShort weight="semibold">Kommentar</BodyShort>
                                <BodyLong>{kommentar.trim()}</BodyLong>
                            </VStack>
                        )}
                    </VStack>
                </Box>

                {sendKlage.isError && (
                    <Alert variant="error">
                        <Heading size="xsmall" spacing>
                            Sending til Kabal feilet
                        </Heading>
                        <BodyShort>
                            {sendKlage.error?.detail ?? "En ukjent feil oppstod. Prøv igjen eller kontakt support."}
                        </BodyShort>
                    </Alert>
                )}

                <HStack gap="space-8" justify="end">
                    <Button
                        type="button"
                        variant="tertiary"
                        onClick={() => setVisBekreftelse(false)}
                        disabled={sendKlage.isPending}
                    >
                        Tilbake
                    </Button>
                    <Button type="button" variant="primary" onClick={onBekreftOgSend} loading={sendKlage.isPending}>
                        Bekreft og send til Kabal
                    </Button>
                </HStack>
            </VStack>
        );
    }

    // ── Step 1: Form ──────────────────────────────────────────────────────────
    return (
        <VStack gap="space-32">
            <Heading size="medium">Send klage til Kabal</Heading>

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

            <DatePicker {...datepickerProps}>
                <DatePicker.Input {...inputProps} label="Dato klage mottatt" error={datoError} />
            </DatePicker>

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

            <Textarea
                label="Kommentar (valgfri)"
                description="Eventuell tilleggsinformasjon til Kabal"
                value={kommentar}
                onChange={(e) => setKommentar(e.target.value)}
                maxLength={MAX_KOMMENTAR_LENGDE}
                rows={4}
            />

            <HStack gap="space-8" justify="end">
                <Button type="button" variant="tertiary" onClick={onAngre}>
                    Angre
                </Button>
                <Button
                    type="button"
                    variant="primary"
                    onClick={onVisBekreftelse}
                    disabled={!sak.rettigheter.includes("SEND_KLAGE")}
                >
                    Send til Kabal
                </Button>
            </HStack>
        </VStack>
    );
}
