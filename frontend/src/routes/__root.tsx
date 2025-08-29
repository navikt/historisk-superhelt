import {createRootRoute, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import "@navikt/ds-css/darkside";
import {Theme} from "@navikt/ds-react/Theme";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {Heading, Page} from "@navikt/ds-react";
import {Header} from "../components/Header";

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

function Footer() {
    return <div id="decorator-footer" >footer</div>;
}