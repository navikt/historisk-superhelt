import { createFileRoute } from '@tanstack/react-router'
import { OppgaveTabell } from '../components/OppgaveTabell'

export const Route = createFileRoute('/')({
  component: Index,
})

function Index() {
  return <OppgaveTabell />
}