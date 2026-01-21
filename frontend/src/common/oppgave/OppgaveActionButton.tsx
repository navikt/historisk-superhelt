import {Button, HStack} from '@navikt/ds-react'
import {Link as RouterLink} from '@tanstack/react-router'
import {OppgaveMedSak} from "@generated";

export function OppgaveActionButton(props: { oppgave: OppgaveMedSak }) {
   const oppgave = props.oppgave
   const saksnummer = oppgave.saksnummer

   const ukjentOppgave = () => {
      return <div />
   }

   const actionButton = (to: string, title: string) => {
      return (
         <Button as={RouterLink} to={to} variant="primary" size="xsmall">
            {title}
         </Button>
      )
   }

   const actionButtonOrDisabled = (to: string, title: string) => {
      if (!saksnummer) {
         return ukjentOppgave()
      }
      return actionButton(to, title)
   }

   const renderButton = () => {
      switch (oppgave.oppgavetype) {
         case 'JFR':
            return actionButton(`/oppgave/${oppgave.oppgaveId}/journalfor`, 'Journalfør')
         case 'BEH_SAK':
            return actionButtonOrDisabled(`/sak/${saksnummer}`, 'Behandle')
         case 'GOD_VED':
            return actionButtonOrDisabled(`/behandling/${saksnummer}/totrinnskontroll`, 'Attester')
      }
      return null
   }

   return <HStack gap="2">{renderButton()}</HStack>
}
