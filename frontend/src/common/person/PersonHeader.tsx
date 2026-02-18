import {Alert, BodyShort, Box, CopyButton, HStack, Link, Tag} from "@navikt/ds-react";
import {PersonIcon} from "@navikt/aksel-icons";
import {Link as RouterLink} from "@tanstack/react-router";
import {useSuspenseQuery} from "@tanstack/react-query";
import {finnPersonQuery} from "~/common/person/person.query";
import {isoTilLokal} from "~/common/dato.utils";

interface Props {
    maskertPersonId: string,
}

export function PersonHeader({maskertPersonId}: Props) {
    const {data: person} = useSuspenseQuery(finnPersonQuery(maskertPersonId))

    //TO DO ikke vis alder for dødsfall, bare dødsdato
    return (
        <Box background="neutral-moderate" padding="space-4">
            <HStack justify={"space-between"}>

                <HStack gap="space-4" align="center" justify={"space-between"}>
                    <PersonIcon fontSize="1.5rem"/>
                    <Link as={RouterLink} to={`/person/${person.maskertPersonident}`} underline={false}>
                        <BodyShort size={"large"}>
                            {person?.navn}{!person.doedsfall && ` (${person.alder??'-'} år)`}
                        </BodyShort>
                    </Link>
                    <BodyShort size={"small"}>{person.fnr}</BodyShort>
                    <CopyButton copyText={person.fnr}/>
                    {person.doedsfall &&
                        <Tag data-color="neutral" variant="outline">Dødsdato: {isoTilLokal(person.doedsfall)}</Tag>}
                    {person.adressebeskyttelseGradering &&
                        <Tag data-color="warning" variant="outline">Adressebeskyttelse: {person.adressebeskyttelseGradering}</Tag>}
                    {person.verge && <Tag data-color="info" variant="outline">Verge</Tag>}
                </HStack>
                {person.avvisningsBegrunnelse &&
                    <Alert variant={"error"} size={"small"}>{person.avvisningsBegrunnelse}</Alert>}
            </HStack>
        </Box>
    );
}