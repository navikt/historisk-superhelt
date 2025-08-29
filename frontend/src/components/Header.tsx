import {InternalHeader, Link, Spacer} from "@navikt/ds-react";
import {Link as RouterLink} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";

async function fetchUser() {
    const response= await fetch("/api/user")

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.text()
}


export function Header() {
    const {  data: username}  = useQuery({ queryKey: ['user'], queryFn: fetchUser })
    return <InternalHeader>
        <InternalHeader.Title as="h1">Super</InternalHeader.Title>

        <Link as={RouterLink} to="/about" >About</Link>
        <Spacer />
        <InternalHeader.User name={username?? "----"} />
    </InternalHeader>
}