import {BodyShort, Heading, HStack, Panel, VStack} from "@navikt/ds-react";
import {PersonIcon} from "@navikt/aksel-icons";
import {Link} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";
import {getUserInfoOptions} from "@api/@tanstack/react-query.gen";
import {getPerson} from "@api";

export function PersonHeader() {
  const {data: person} = useQuery({
    ...getPerson()
  })
  // Mock data
  const person = {
    fnr: '12345678901',
    navn: 'Ola Nordmann',
    adresse: 'Storgata 1, 0123 Oslo',
    telefon: '12345678',
    epost: 'ola.nordmann@example.com',
    sivilstand: 'Gift',
    barn: 2
  }
  return <>
    {/* Personinformasjon */}
    <Panel border>
      <HStack gap="4" align="start">
       <PersonIcon fontSize="3rem"/>
        <VStack gap="4">
          <Link to="/person"><Heading size="medium">{person.navn}</Heading></Link>
          <HStack gap="8">
            <VStack gap="1">
              <BodyShort size="small"><strong>Fødselsnummer:</strong> {person.fnr}</BodyShort>
              <BodyShort size="small"><strong>Adresse:</strong> {person.adresse}</BodyShort>
              <BodyShort size="small"><strong>Telefon:</strong> {person.telefon}</BodyShort>
            </VStack>
            <VStack gap="1">
              <BodyShort size="small"><strong>E-post:</strong> {person.epost}</BodyShort>
              <BodyShort size="small"><strong>Sivilstand:</strong> {person.sivilstand}</BodyShort>
              <BodyShort size="small"><strong>Antall barn:</strong> {person.barn}</BodyShort>
            </VStack>
          </HStack>
        </VStack>
      </HStack>
    </Panel>
  </>;
}