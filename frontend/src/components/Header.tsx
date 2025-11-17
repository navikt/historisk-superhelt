import {BodyShort, Detail, Dropdown, HStack, InternalHeader, Link, Search, Spacer} from "@navikt/ds-react";
import {Link as RouterLink, useNavigate} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";
import {LeaveIcon} from "@navikt/aksel-icons";
import {getUserInfoOptions} from "@generated/@tanstack/react-query.gen";
import {useState} from "react";
import {findPersonByFnr as findPerson} from "@generated";

export function Header() {
    const [search, setSearch] = useState<string>();
    const [searchError, setSearchError] = useState<string>();
    const navigate = useNavigate()

    // const {  data: username}  = useQuery({ queryKey: ['user'], queryFn: fetchUser })
    const {data: user} = useQuery({
        ...getUserInfoOptions()
    })

    async function doSearch() {
        setSearchError(undefined)
        if (search?.length != 11) {
            setSearchError("Ugyldig fødselsnummer")
        }

        const {data, error} = await findPerson({
                body: {fnr: search!}
            }
        )
        if (error) {
            setSearchError("Noe gikk galt " + error)
        }
        await navigate({to: "/person/$personid", params: {personid: data?.maskertPersonident!}})
        setSearch("")
        setSearchError(undefined)

    }

    return <InternalHeader>
        <InternalHeader.Title as="h1">
            <Link as={RouterLink} to={"/"} underline={false} variant={"neutral"}><img src="/logo.svg" height="35rem"
                                                                                      alt={""}/>Superhelt</Link>
        </InternalHeader.Title>
        <HStack
            as="form"
            paddingInline="space-20"
            align="center"
            onSubmit={(e) => {
                e.preventDefault();
                doSearch();
            }}
        >
            <Search
                label="Søk"
                size="small"
                variant="simple"
                placeholder="Finn person"
                value={search}
                onChange={setSearch}
                error={searchError}
            />
        </HStack>
        <Spacer/>

        <Dropdown>
            <InternalHeader.UserButton
                as={Dropdown.Toggle}
                name={user?.name ?? "--"}
                description="Enhet: Skien"
            />
            <Dropdown.Menu>
                <dl>
                    <BodyShort as="dt" size="small">
                        {user?.name}
                    </BodyShort>
                    <Detail as="dd">Roller: {user?.roles}</Detail>
                </dl>
                <Dropdown.Menu.Divider/>
                <Dropdown.Menu.List>
                    <Dropdown.Menu.List.Item as="a" href="/oauth2/logout">
                        Logg ut <Spacer/> <LeaveIcon aria-hidden fontSize="1.5rem"/>
                    </Dropdown.Menu.List.Item>
                </Dropdown.Menu.List>
            </Dropdown.Menu>
        </Dropdown>
    </InternalHeader>
}