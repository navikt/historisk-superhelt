import { createFileRoute } from '@tanstack/react-router'

async function fetchUser() {
    const response= await fetch("/api/user")

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.text()
}

export const Route = createFileRoute('/about')({
    loader: () => fetchUser(),
    component: About,
})

function About() {
    const user = Route.useLoaderData()
    return <div className="p-2">Hallo  {user} </div>
}