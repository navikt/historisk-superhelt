import { createFileRoute } from '@tanstack/react-router'

export const Route = createFileRoute('/OppgaveTable')({
  component: RouteComponent,
})

function RouteComponent() {
  return <div>Hello "/OppgaveTable"!</div>
}
