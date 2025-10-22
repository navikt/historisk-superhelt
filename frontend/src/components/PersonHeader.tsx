import {Alert, BodyShort, Box, Heading, HStack, Tag, VStack, Link} from "@navikt/ds-react";
import {PersonIcon} from "@navikt/aksel-icons";
import {Link as RouterLink} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";
import {getPersonByMaskertIdentOptions} from "@api/@tanstack/react-query.gen";

interface Props {
    maskertPersonId: string
}

export function PersonHeader({maskertPersonId}: Props) {
  const {data: person} = useQuery(
      {...getPersonByMaskertIdentOptions({
          path:{
            maskertPersonident: maskertPersonId
          }
      })
  })

  return <Box padding="4" borderWidth="1" borderRadius="small">
      <HStack gap="4" align="start">
       <PersonIcon fontSize="3rem"/>
        <VStack gap="4">
          <Link as={RouterLink} to={"/person/" + person?.maskertPersonident} underline={false} variant={"neutral"}><Heading size="medium">{person?.navn}</Heading></Link>
          <HStack gap="8">
            <VStack gap="1">
              <BodyShort size="small"><strong>Fødselsnummer:</strong> {person?.fnr}</BodyShort>
              <BodyShort size="small"><strong>Adressegradering:</strong>  {person?.adressebeskyttelseGradering && <Tag variant={"info"} >{person?.adressebeskyttelseGradering}</Tag>}</BodyShort>
                <BodyShort size="small"><strong>Verge:</strong> {person?.verge}</BodyShort>

            </VStack>
            <VStack gap="1">
                {person?.avvisningsBegrunnelse && <Alert variant={"error"} size={"small"}>{person?.avvisningsBegrunnelse}</Alert>}
              {/*<BodyShort size="small"><strong>E-post:</strong> {person.epost}</BodyShort>*/}
              {/*<BodyShort size="small"><strong>Sivilstand:</strong> {person.sivilstand}</BodyShort>*/}
              {/*<BodyShort size="small"><strong>Antall barn:</strong> {person.barn}</BodyShort>*/}
            </VStack>
          </HStack>
        </VStack>
      </HStack>
    </Box>
}