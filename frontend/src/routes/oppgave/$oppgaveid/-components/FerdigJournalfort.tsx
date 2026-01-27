import {Alert, BodyShort, Heading, HStack, Link} from '@navikt/ds-react'
import {Link as RouterLink} from '@tanstack/react-router'

interface Props {
   saksnummer?: string
}

export function FerdigJournalfort({ saksnummer }: Props) {
   return (
      <Alert variant={'success'}>
         <HStack gap={'space-16'}>
            <Heading size={'small'} level="3">
               Ferdig journalført
            </Heading>
            <BodyShort>Journalposten er ferdig og knyttet til behandling {saksnummer}</BodyShort>
            <Link as={RouterLink} to={`/sak/${saksnummer}`}>
               Gå til behandling
            </Link>
         </HStack>
      </Alert>
   )
}
