import { render, screen, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import {
    createMemoryHistory,
    createRootRoute,
    createRoute,
    createRouter,
    RouterProvider,
} from "@tanstack/react-router";
import { describe, expect, it } from "vitest";
import { ProcessMenu } from "./ProcessMenu";
import { StepType } from "./StepType";

function renderWithRouter(ui: React.ReactNode, { initialLocation = "/" } = {}) {
    const rootRoute = createRootRoute({ component: () => ui });
    const indexRoute = createRoute({ getParentRoute: () => rootRoute, path: "/" });
    const testRoute = createRoute({ getParentRoute: () => rootRoute, path: "/test" });
    const sakRoute = createRoute({ getParentRoute: () => rootRoute, path: "/sak/$saksnummer/oppsummering" });
    const routeTree = rootRoute.addChildren([indexRoute, sakRoute, testRoute]);

    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    const router = createRouter({
        routeTree,
        history: createMemoryHistory({ initialEntries: [initialLocation] }),
    });

    return {
        router,
        ...render(
            <QueryClientProvider client={queryClient}>
                <RouterProvider router={router} />
            </QueryClientProvider>,
        ),
    };
}

describe("ProcessMenu", () => {
    it("renders children", async () => {
        renderWithRouter(
            <ProcessMenu>
                <span data-testid="child">Hello</span>
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByTestId("child")).toBeDefined());
    });

    it("renders ProcessMenuItem with label", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Oppsummering" to="/" />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByText("Oppsummering")).toBeDefined());
    });

    it("renders multiple items with correct href", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Steg 1" to="/" />
                <ProcessMenu.Item label="Steg 2" to="/sak/$saksnummer/oppsummering" params={{ saksnummer: "123" }} />
                <ProcessMenu.Item label="Steg 3" to={"/test" as "/"} />
            </ProcessMenu>,
        );
        await waitFor(() => {
            expect(screen.getByLabelText("Steg 1").getAttribute("href")).toBe("/");
            expect(screen.getByLabelText("Steg 2").getAttribute("href")).toBe("/sak/123/oppsummering");
            expect(screen.getByLabelText("Steg 3").getAttribute("href")).toBe("/test");
        });
    });

    it("sets aria-label with deaktivert when disabled", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Vedtak" to="/" disabled />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByLabelText("Vedtak (deaktivert)")).toBeDefined());
    });

    it("sets aria-label to label when not disabled", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Vedtak" to="/" />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByLabelText("Vedtak")).toBeDefined());
    });

    it("renders StepIcon for warning step type", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Advarsel" to="/" stepType={StepType.warning} />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByText("Advarsel")).toBeDefined());
    });

    it("renders StepIcon for success step type", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Ferdig" to="/" stepType={StepType.success} />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByText("Ferdig")).toBeDefined());
    });

    it("renders StepIcon for danger step type", async () => {
        renderWithRouter(
            <ProcessMenu>
                <ProcessMenu.Item label="Feil" to="/" stepType={StepType.danger} />
            </ProcessMenu>,
        );
        await waitFor(() => expect(screen.getByText("Feil")).toBeDefined());
    });
});
