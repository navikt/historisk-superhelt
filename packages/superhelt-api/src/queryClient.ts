import { QueryClient } from "@tanstack/react-query";
import { client } from "./client.gen";
import type { ProblemDetail } from "./types.gen";

class ApiError extends Error {
    constructor(
        readonly status: number,
        problem: ProblemDetail,
    ) {
        super(problem.detail || problem.title || `HTTP ${status}`);
        Object.assign(this, problem, { status });
    }
}
// Generert kode returnerer
client.interceptors.error.use((error, response) => {
    if (!response || response.ok) {
        return error;
    }

    const problem =
        typeof error === "object" && error !== null
            ? error
            : { detail: typeof error === "string" ? error : response.statusText };

    return new ApiError(response.status, problem);
});

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: (failureCount, error) => {
                if (error instanceof ApiError && error.status >= 400 && error.status < 500 && error.status !== 429) {
                    return false;
                }
                return failureCount < 3;
            },
        },
    },
});
