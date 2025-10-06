import {BodyShort, Detail, Dropdown, HStack, InternalHeader, Search, Spacer} from "@navikt/ds-react";
import {Link as RouterLink, useNavigate} from "@tanstack/react-router";
import {useQuery} from "@tanstack/react-query";
import {LeaveIcon} from "@navikt/aksel-icons";
import {getUserInfoOptions} from "@api/@tanstack/react-query.gen";
import {useState} from "react";
import {findPerson} from "@api";

interface Props {
    maskertPersonId: string
}

export function Header() {
    const [search, setSearch] = useState<string>();
    const [seachError, setSearchError] = useState<string>();
    const navigate= useNavigate()

    // const {  data: username}  = useQuery({ queryKey: ['user'], queryFn: fetchUser })
    const {data: user} = useQuery({
        ...getUserInfoOptions()
    })

    async function doSeach() {
        setSearchError(undefined)
        if (search?.length != 11) {
            setSearchError("Ugyldig fødselsnummer")
        }

        const {data, error} = await findPerson({
                body: {fnr: search!}
            }
        )
        if(error){
            setSearchError("Noe gikk galt "+ error)
        }
        console.log(data)
        await navigate({to:"/person/$personid", params:{ personid: data?.maskertPersonident!}})
        setSearch("")
        setSearchError(undefined)

    }

    return <InternalHeader>
        <InternalHeader.Title as="h1">
            <RouterLink to={"/"}><img src="logo.svg" height="35rem"/>Superhelt</RouterLink>

        </InternalHeader.Title>
        <HStack
            as="form"
            paddingInline="space-20"
            align="center"
            onSubmit={(e) => {
                e.preventDefault();
                doSeach();
            }}
        >
            <Search
                label="Søk"
                size="small"
                variant="simple"
                placeholder="Finn person"
                value={search}
                onChange={setSearch}
                error={seachError}
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
                    <Detail as="dd">123123</Detail>
                </dl>
                <Dropdown.Menu.Divider/>
                <Dropdown.Menu.List>
                    <Dropdown.Menu.List.Item>
                        Logg ut <Spacer/> <LeaveIcon aria-hidden fontSize="1.5rem"/>
                    </Dropdown.Menu.List.Item>
                </Dropdown.Menu.List>
            </Dropdown.Menu>
        </Dropdown>
    </InternalHeader>
}