import { FigureCombinationIcon } from "@navikt/aksel-icons";
import { Alert, Bleed, BodyShort, Box, CopyButton, HStack, Link, Tag, Tooltip } from "@navikt/ds-react";
import { useSuspenseQuery } from "@tanstack/react-query";
import { Link as RouterLink } from "@tanstack/react-router";
import { isoTilLokal } from "~/common/dato.utils";
import { finnPersonQuery } from "~/common/person/person.query";
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
        <Bleed marginInline="full" reflectivePadding asChild>
            <Box
                background="neutral-moderate"
                borderColor="neutral-subtle"
                borderWidth="0 0 1 0"
                height="3.25rem"
                asChild
            >
                <HStack gap="space-8" align="center" justify="start">
                    <HStack gap="space-12" align="center" justify="space-between">
                        <HStack gap="space-4" align="center" justify="start">
                            <FigureCombinationIcon fontSize="1.75rem" />
                            <BodyShort weight="semibold">
                                {person?.navn}
                                {!person.doed && ` (${person.alder ?? "-"} år)`}
                            </BodyShort>
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
                    {person.doed && (
                        <Tooltip content={`Brukeren døde ${isoTilLokal(person.doedsfall)}`} placement="bottom">
                            <Tag data-color="neutral" variant="outline" size="small">
                                Dødsdato: {isoTilLokal(person.doedsfall)}
                            </Tag>
                        </Tooltip>
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
        </Bleed>
    );
}
