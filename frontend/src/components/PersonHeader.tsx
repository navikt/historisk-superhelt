import {Alert, BodyShort, Box, CopyButton, HStack, Link, Tag} from "@navikt/ds-react";
import {PersonIcon} from "@navikt/aksel-icons";
import {Link as RouterLink} from "@tanstack/react-router";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getPersonByMaskertIdentOptions} from "@generated/@tanstack/react-query.gen";

interface Props {
    maskertPersonId: string,
}

export function PersonHeader({maskertPersonId}: Props) {
    const {data: person} = useSuspenseQuery(
        {
            ...getPersonByMaskertIdentOptions({
                path: {
                    maskertPersonident: maskertPersonId
                }
            })
        })

    return <Box.New background="neutral-moderate" padding="space-4">
        <HStack justify={"space-between"}>

            <HStack gap="space-4" align="center" justify={"space-between"}>
                <PersonIcon fontSize="1.5rem"/>
                <Link as={RouterLink} to={"/person/" + person.maskertPersonident} underline={false}>
                    <BodyShort size={"large"}>
                        {person?.navn}
                        {person?.alder !== undefined && person?.alder !== null &&
                            ` (${person.alder} år)`}
                    </BodyShort>
                </Link>
                <BodyShort size={"small"}>{person.fnr}</BodyShort>
                <CopyButton copyText={person.fnr}/>
            </HStack>
            {person.avvisningsBegrunnelse &&
                <Alert variant={"error"} size={"small"}>{person.avvisningsBegrunnelse}</Alert>}
            <HStack gap="space-16" align="center">
                {/*TODO Her kommer mer info om person,*/}

                {person.adressebeskyttelseGradering &&
                    <Tag variant={"warning"}>Adressebeskyttelse: {person.adressebeskyttelseGradering}</Tag>}
                {person.verge && <Tag variant={"info"}>Verge: {person.verge}</Tag>}
            </HStack>

        </HStack>
    </Box.New>
}