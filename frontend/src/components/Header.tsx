import {InternalHeader, Link, Spacer} from "@navikt/ds-react";
import {Link as RouterLink} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";

import {getUserInfoOptions} from "../gen/@tanstack/react-query.gen"

async function fetchUser() {
    const response= await fetch("/api/user")

    if (!response.ok) {
        throw new Error('Network response was not ok');
    }
    return response.json()
}


export function Header() {
    // const {  data: username}  = useQuery({ queryKey: ['user'], queryFn: fetchUser })
    const {  data: user}  = useQuery({
        ...getUserInfoOptions()
    })
    return <InternalHeader>
        <InternalHeader.Title as="h1">Super</InternalHeader.Title>

        <Link as={RouterLink} to="/about" >About</Link>
        <Spacer />
        <InternalHeader.User name={user?.name?? "----"} />
    </InternalHeader>
}