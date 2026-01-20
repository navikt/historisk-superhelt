import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/oppgave/$oppgaveid/journalfor')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/oppgave/$oppgaveid/journalfor"!</div>
}
