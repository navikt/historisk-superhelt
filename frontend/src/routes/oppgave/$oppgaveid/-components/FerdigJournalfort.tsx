import {Alert, BodyShort, Heading, HStack, Link} from '@navikt/ds-react'
import {Link as RouterLink} from '@tanstack/react-router'

interface Props {
   behandlingsnummer?: string
}

export function FerdigJournalfort({ behandlingsnummer }: Props) {
   return (
      <Alert variant={'success'}>
         <HStack gap={'space-16'}>
            <Heading size={'small'} level="3">
               Ferdig journalført
            </Heading>
            <BodyShort>Journalposten er ferdig og knyttet til behandling {behandlingsnummer}</BodyShort>
            <Link as={RouterLink} to={`/behandling/${behandlingsnummer}`}>
               Gå til behandling
            </Link>
         </HStack>
      </Alert>
   )
}
