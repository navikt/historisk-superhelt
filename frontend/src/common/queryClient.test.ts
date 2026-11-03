import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { queryClient } from "./queryClient";

describe("queryClient retry configuration", () => {
    const defaultOptions = queryClient.getDefaultOptions();

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
    });

    it.each([400, 401, 403, 404, 408, 422, 499])("does not retry HTTP %i errors", async (status) => {
        let attempts = 0;
        const error = { status };

        await expect(
            queryClient.query({
                queryKey: ["retry-test", status],
                queryFn: async () => {
                    attempts += 1;
                    throw error;
                },
            }),
        ).rejects.toEqual(error);

        expect(attempts).toBe(1);
    });

    it.each([
        ["HTTP 429", { status: 429 }],
        ["HTTP 500", { status: 500 }],
        ["network errors", new Error("Network error")],
        ["unknown status", { message: "Unknown error" }],
        ["non-numeric status", { status: "401" }],
        ["null error payload", null],
        ["undefined error payload", undefined],
        ["string error payload", "Unknown error"],
    ])("retries %s up to four total attempts", async (_description, error) => {
        let attempts = 0;

        await expect(
            queryClient.query({
                queryKey: ["retry-test", _description],
                queryFn: async () => {
                    attempts += 1;
                    throw error;
                },
            }),
        ).rejects.toBe(error);

        expect(attempts).toBe(4);
    });

    it("succeeds after a transient failure", async () => {
        let attempts = 0;

        await expect(
            queryClient.query({
                queryKey: ["retry-test", "transient"],
                queryFn: async () => {
                    attempts += 1;

                    if (attempts === 1) {
                        throw { status: 500 };
                    }

                    return "success";
                },
            }),
        ).resolves.toBe("success");

        expect(attempts).toBe(2);
    });
});
