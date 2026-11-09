import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getUserInfoOptions } from "./@tanstack/react-query.gen";
import { client } from "./client.gen";
import { queryClient } from "./queryClient";
import type { GetUserInfoResponse } from "./types.gen";

describe("API query client retry configuration", () => {
    const defaultOptions = queryClient.getDefaultOptions();
    const defaultClientConfig = client.getConfig();
    const userInfo: GetUserInfoResponse = {
        name: "Test User",
        ident: "T123456",
        roles: ["LES"],
        enheter: [],
        tema: ["HEL"],
    };

    beforeEach(() => {
        queryClient.setDefaultOptions({
            ...defaultOptions,
            queries: {
                ...defaultOptions.queries,
                gcTime: 0,
                retryDelay: 0,
            },
        });
    });

    afterEach(() => {
        queryClient.clear();
        queryClient.setDefaultOptions(defaultOptions);
        client.setConfig(defaultClientConfig);
    });

    it.each([400, 401, 403])("does not retry a bodyless HTTP %i error", async (status) => {
        const fetch = vi.fn(async () => new Response(null, { status }));
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).rejects.toMatchObject({ status });

        expect(fetch).toHaveBeenCalledTimes(1);
    });

    it("preserves ProblemDetail fields while using the actual HTTP status", async () => {
        const problemDetail = {
            status: 800,
            title: "Not authorized",
            detail: "The request is not allowed",
            instance: "/user",
        };
        const fetch = vi.fn(
            async () =>
                new Response(JSON.stringify(problemDetail), {
                    status: 403,
                    headers: { "Content-Type": "application/problem+json" },
                }),
        );
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).rejects.toMatchObject({
            ...problemDetail,
            status: 403,
        });

        expect(fetch).toHaveBeenCalledTimes(1);
    });

    it("keeps a text response as the error message", async () => {
        const fetch = vi.fn(
            async () =>
                new Response("Request rejected", {
                    status: 400,
                    headers: { "Content-Type": "text/plain" },
                }),
        );
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).rejects.toMatchObject({
            message: "Request rejected",
            status: 400,
        });

        expect(fetch).toHaveBeenCalledTimes(1);
    });

    it.each([429, 500])("retries an HTTP %i error four total times", async (status) => {
        const fetch = vi.fn(async () => new Response(null, { status }));
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).rejects.toMatchObject({ status });

        expect(fetch).toHaveBeenCalledTimes(4);
    });

    it.each([429, 500])("returns the API result when retrying HTTP %i succeeds", async (status) => {
        const fetch = vi
            .fn<typeof globalThis.fetch>()
            .mockResolvedValueOnce(new Response(null, { status }))
            .mockResolvedValueOnce(
                new Response(JSON.stringify(userInfo), {
                    status: 200,
                    headers: { "Content-Type": "application/json" },
                }),
            );
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).resolves.toEqual(userInfo);

        expect(fetch).toHaveBeenCalledTimes(2);
    });

    it("preserves the original network error", async () => {
        const networkError = new Error("Network error");
        const fetch = vi.fn(async () => {
            throw networkError;
        });
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).rejects.toBe(networkError);

        expect(fetch).toHaveBeenCalledTimes(4);
    });

    it("retries an unrelated error with a status field four total times", async () => {
        let attempts = 0;
        const error = { status: 400 };

        await expect(
            queryClient.query({
                queryKey: ["unrelated-status-error"],
                queryFn: async () => {
                    attempts += 1;
                    throw error;
                },
            }),
        ).rejects.toBe(error);

        expect(attempts).toBe(4);
    });

    it("returns a successful query result", async () => {
        const fetch = vi.fn(
            async () =>
                new Response(JSON.stringify(userInfo), {
                    status: 200,
                    headers: { "Content-Type": "application/json" },
                }),
        );
        client.setConfig({ baseUrl: "http://localhost", fetch });

        await expect(queryClient.query(getUserInfoOptions())).resolves.toEqual(userInfo);

        expect(fetch).toHaveBeenCalledTimes(1);
    });
});
