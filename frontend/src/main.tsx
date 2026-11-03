import { createRouter, RouterProvider } from "@tanstack/react-router";
import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import "@navikt/ds-css";
import "~/global.css";
import type { QueryClient } from "@tanstack/react-query";
import { QueryClientProvider } from "@tanstack/react-query";
import { queryClient } from "./common/queryClient";
import { initialiserFaro } from "./common/telemetry.utils";
// Import the generated route tree
import { routeTree } from "./routeTree.gen";

export interface RouterContext {
    queryClient: QueryClient;
}

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

initialiserFaro(router);

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
