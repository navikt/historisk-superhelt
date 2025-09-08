import {BodyShort, Detail, Dropdown, HStack, InternalHeader, Link, Search, Spacer} from "@navikt/ds-react";
import {Link as RouterLink} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";

import {getUserInfoOptions} from "../gen/@tanstack/react-query.gen"
import {LeaveIcon} from "@navikt/aksel-icons";



export function Header() {
    // const {  data: username}  = useQuery({ queryKey: ['user'], queryFn: fetchUser })
    const {  data: user}  = useQuery({
        ...getUserInfoOptions()
    })
    return <InternalHeader>
        <InternalHeader.Title as="h1"><img src="logo.svg" height="35rem"/>superhelt</InternalHeader.Title>
        <HStack
            as="form"
            paddingInline="space-20"
            align="center"
            onSubmit={(e) => {
                e.preventDefault();
                console.info("Search!");
            }}
        >
            <Search
                label="Søk"
                size="small"
                variant="simple"
                placeholder="Finn person"
            />
        </HStack>
        <Spacer />

        <Dropdown >
            <InternalHeader.UserButton
                as={Dropdown.Toggle}
                name={user?.name?? "--"}
                description="Enhet: Skien"
            />
            <Dropdown.Menu>
                <dl>
                    <BodyShort as="dt" size="small">
                        {user?.name}
                    </BodyShort>
                    <Detail as="dd">123123</Detail>
                </dl>
                <Dropdown.Menu.Divider />
                <Dropdown.Menu.List>
                    <Dropdown.Menu.List.Item>
                        Logg ut <Spacer /> <LeaveIcon aria-hidden fontSize="1.5rem" />
                    </Dropdown.Menu.List.Item>
                </Dropdown.Menu.List>
            </Dropdown.Menu>
        </Dropdown>
    </InternalHeader>
}