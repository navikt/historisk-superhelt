import { getWebInstrumentations, initializeFaro } from "@grafana/faro-web-sdk";
import { createRouter, RouterProvider } from "@tanstack/react-router";
import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import "@navikt/ds-css";
import "~/global.css";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
// Import the generated route tree
import { routeTree } from "./routeTree.gen";

export interface RouterContext {
    queryClient: QueryClient;
}

const queryClient = new QueryClient();

// Set up a Router instance
const router = createRouter({
    routeTree,
    context: {
        queryClient,
    } as RouterContext,
    defaultPreload: "intent",
    // Since we're using React Query, we don't want loader calls to ever be stale
    // This will ensure that the loader is always called when the route is preloaded or visited
    defaultPreloadStaleTime: 0,
    scrollRestoration: true,
});

initializeFaro({
    url: "https://telemetry.ekstern.dev.nav.no/collect",
    app: {
        name: "superhelt",
        namespace: "historisk",
        version: process.env.COMMIT_SHA || "local",
    },
    instrumentations: [...getWebInstrumentations()],
    beforeSend: (item) => {
        if (item.meta?.page?.url) {
            try {
                const url = new URL(item.meta.page.url);
                url.search = "";
                item.meta.page.url = url.toString();
            } catch {
                /* ignore malformed URLs */
            }
        }
        const payload = JSON.stringify(item);
        if (/(?<!\d)\d{11}(?!\d)/.test(payload)) {
            return null;
        }
        return item;
    },
    pageTracking: {
        generatePageId: (location) =>
            location.pathname
                .replace(/\/sak\/[^/]+/, "/sak/{saksnummer}")
                .replace(/\/person\/[^/]+/, "/person/{personid}")
                .replace(/\/oppgave\/[^/]+/, "/oppgave/{oppgaveid}"),
    },
});

// Render the app
const rootElement = document.getElementById("root")!;
if (!rootElement.innerHTML) {
    const root = ReactDOM.createRoot(rootElement);
    root.render(
        <StrictMode>
            <QueryClientProvider client={queryClient}>
                <RouterProvider router={router} />
            </QueryClientProvider>
        </StrictMode>,
    );
}
