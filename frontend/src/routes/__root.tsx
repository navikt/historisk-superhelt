import { createRootRoute, Link as RouterLink, Outlet } from '@tanstack/react-router'
import { TanStackRouterDevtools } from '@tanstack/react-router-devtools'
import "@navikt/ds-css/darkside";
import { Theme } from "@navikt/ds-react/Theme";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {Heading, InternalHeader, Link, Page, Spacer} from "@navikt/ds-react";

export const Route = createRootRoute({
    component: () => (
        <Theme theme={"light"}>
            <Page footer={<Footer />}>
                <Page.Block as="main" width="xl" gutters>
                        <Heading level="1" size="large">
                            <Header />
                        </Heading>
                        <Outlet />
                </Page.Block>
            </Page>

            <hr />

            <TanStackRouterDevtools />
            <ReactQueryDevtools buttonPosition="bottom-right" />
        </Theme>
    ),
})

function Header() {
    return <InternalHeader>
        <InternalHeader.Title as="h1">Super</InternalHeader.Title>
        <Spacer />
        <Link as={RouterLink} to="/about" >About</Link>
        <InternalHeader.User name="Petter Normann" />
    </InternalHeader>
}

function Footer() {
    return <div id="decorator-footer" >footer</div>;
}