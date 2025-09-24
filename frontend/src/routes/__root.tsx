import {createRootRoute, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import "@navikt/ds-css/darkside";
import {Theme} from "@navikt/ds-react/Theme";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {Heading, Page} from "@navikt/ds-react";
import {Header} from "../components/Header";
import {RfcErrorBoundary} from "../components/error/RfcErrorBoundry";

export const Route = createRootRoute({
    component: () => (
        <Theme theme={"light"}>
            <Page footer={<Footer />} >
                <RfcErrorBoundary >
                <Page.Block as="header" width="2xl" gutters>
                    <Header />
                </Page.Block>
                <Page.Block as="main" width="2xl" gutters>
                    <Outlet />
                </Page.Block>
                </RfcErrorBoundary >
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