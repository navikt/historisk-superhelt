import {createFileRoute} from '@tanstack/react-router'
import {SaksbehandlersOppgaveTabell} from './-components/SaksbehandlersOppgaveTabell'

export const Route = createFileRoute('/')({
    component: Index,
})

function Index() {
    return <SaksbehandlersOppgaveTabell/>
}