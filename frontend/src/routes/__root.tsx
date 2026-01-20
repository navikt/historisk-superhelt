import {createRootRouteWithContext, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import {Theme} from "@navikt/ds-react/Theme";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {Box, GlobalAlert, Page} from "@navikt/ds-react";
import {Header} from "./-components/Header";
import {RfcErrorBoundary} from "~/common/error/RfcErrorBoundary";
import {RouterContext} from "~/main";
import {useSuspenseQuery} from "@tanstack/react-query";
import {getUserInfoOptions} from "@generated/@tanstack/react-query.gen";

export const Route = createRootRouteWithContext<RouterContext>()({
    component: RootComponent,
})

function RootComponent() {

    return <Theme theme={"light"}>
        <Page footer={<Footer/>}>
            <RfcErrorBoundary>
                <Page.Block as="header" gutters>
                    <Header/>
                </Page.Block>
                <Page.Block as="main" gutters>
                    <MainContent/>
                </Page.Block>
            </RfcErrorBoundary>
        </Page>
        <TanStackRouterDevtools/>
        <ReactQueryDevtools buttonPosition="bottom-right"/>
    </Theme>;
}

function MainContent() {
    const {data: user} = useSuspenseQuery({
        ...getUserInfoOptions()
    })

    if (user.roles.length === 0) {
        return <Box padding={"space-8"}>
            <GlobalAlert status="warning">
                <GlobalAlert.Header>
                    <GlobalAlert.Title>
                        Manglende tilgang
                    </GlobalAlert.Title>
                </GlobalAlert.Header>
                <GlobalAlert.Content>
                    <p>Din bruker har ikke noen roller i systemet og har derfor ikke tilgang.</p>

                    <p>Vennligst kontakt systemadministrator for å få tildelt roller.</p>
                </GlobalAlert.Content>
            </GlobalAlert>
        </Box>
    }
    return <Outlet/>;
}

function Footer() {
    return <Page.Block as={"footer"} id="decorator-footer"></Page.Block>;
}