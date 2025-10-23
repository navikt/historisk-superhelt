import { createRootRouteWithContext, Outlet} from '@tanstack/react-router'
import {TanStackRouterDevtools} from '@tanstack/react-router-devtools'
import {Theme} from "@navikt/ds-react/Theme";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {Page} from "@navikt/ds-react";
import {Header} from "~/components/Header";
import {RfcErrorBoundary} from "~/components/error/RfcErrorBoundary";
import {RouterContext} from "~/main";

export const Route = createRootRouteWithContext<RouterContext>()({
    component: () => (
        <Theme theme={"light"}>
            <Page footer={<Footer />}  >
                <RfcErrorBoundary >
                <Page.Block as="header"  gutters>
                    <Header />
                </Page.Block>
                <Page.Block as="main" gutters>
                    <Outlet />
                </Page.Block>
                </RfcErrorBoundary >
            </Page>
            <TanStackRouterDevtools />
            <ReactQueryDevtools buttonPosition="bottom-right" />
        </Theme>
    ),
})

function Footer() {
    return <Page.Block as={"footer"} id="decorator-footer" ></Page.Block>;
}