import { Alert, BodyShort, Box, CopyButton, HStack, Link, Tag, Tooltip } from "@navikt/ds-react";
import { PersonIcon } from "@navikt/aksel-icons";
import { Link as RouterLink } from "@tanstack/react-router";
import { useSuspenseQuery } from "@tanstack/react-query";
import { finnPersonQuery } from "~/common/person/person.query";
import { isoTilLokal } from "~/common/dato.utils";
import { enumkodeTilTekst } from "~/common/string.utils";

interface Props {
    maskertPersonId: string;
}

function adressebeskyttelseVariant(gradering: string | undefined) {
    switch (gradering) {
        case "FORTROLIG":
            return "warning";
        case "STRENGT_FORTROLIG":
        case "STRENGT_FORTROLIG_UTLAND":
            return "danger";
        default:
            return "neutral";
    }
}

export function PersonHeader({ maskertPersonId }: Props) {
    const { data: person } = useSuspenseQuery(finnPersonQuery(maskertPersonId));
    const harBeskyttetAdresse = person.adressebeskyttelseGradering && person.adressebeskyttelseGradering !== "UGRADERT";

    return (
        <Box
            background="neutral-moderate"
            paddingInline="space-24 space-0"
            height="3.5rem"
            style={{ marginInline: "calc(var(--__axc-page-padding-inline) * -1)" }} // Negativ margin for å kompensere for gutter på Page.Block
            asChild
        >
            <HStack gap="space-8" align="center" justify="start">
                <HStack gap="space-12" align="center" justify="space-between">
                    <HStack gap="space-4" align="center" justify="start">
                        <PersonIcon fontSize="1.5rem" />
                        <BodyShort weight="semibold">
                            {person?.navn}
                            {!person.doed && ` (${person.alder ?? "-"} år)`}
                        </BodyShort>
                        {person.doed && (
                            <Tooltip content={`Brukeren døde ${isoTilLokal(person.doedsfall)}`} placement="bottom">
                                <Tag data-color="meta-purple" variant="outline" size="small">
                                    Død
                                </Tag>
                            </Tooltip>
                        )}
                    </HStack>
                    <BodyShort>/</BodyShort>
                    <HStack gap="space-2" align="center" justify="start">
                        <Link as={RouterLink} to={`/person/${person.maskertPersonident}`}>
                            {person.fnr}
                        </Link>
                        <Tooltip content="Kopier fødselsnummer" placement="bottom">
                            <CopyButton size="small" copyText={person.fnr} />
                        </Tooltip>
                    </HStack>
                </HStack>

                {person.avvisningsBegrunnelse && (
                    <Alert variant="error" size="small">
                        {person.avvisningsBegrunnelse}
                    </Alert>
                )}
                {harBeskyttetAdresse && (
                    <Tooltip
                        content={`Brukeren har adressebeskyttelse med gradering ${enumkodeTilTekst(person.adressebeskyttelseGradering, false)}`}
                        placement="bottom"
                    >
                        <Tag
                            data-color={adressebeskyttelseVariant(person.adressebeskyttelseGradering)}
                            variant="outline"
                            size="small"
                        >
                            {enumkodeTilTekst(person.adressebeskyttelseGradering)}
                        </Tag>
                    </Tooltip>
                )}
                {person.harVerge && (
                    <Tooltip
                        content={`${person.vergeInfo?.navn} er verge eller fullmektig for brukeren`}
                        placement="bottom"
                    >
                        <Tag data-color="info" variant="outline" size="small">
                            Verge: {person.vergeInfo?.navn}
                        </Tag>
                    </Tooltip>
                )}
            </HStack>
        </Box>
    );
}
