import {createFileRoute} from '@tanstack/react-router'
import {OppgaveTable} from "~/routes/OppgaveTable";

export const Route = createFileRoute('/')({
    component: Index,
})

function Index() {
    return (
        <OppgaveTable/>

    )
}