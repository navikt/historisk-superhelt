import {createFileRoute, useNavigate} from '@tanstack/react-router'
import {useEffect} from "react";

export const Route = createFileRoute('/sak/$saksnummer/')({
    component: SakIndex,
})

function SakIndex() {
    const {saksnummer} = Route.useParams()
    const navigate = useNavigate();

    useEffect(() => {
        navigate({to: "/sak/$saksnummer/soknad", params: {saksnummer}, replace: true});
    }, [saksnummer])

}
