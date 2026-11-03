import { QueryClient } from "@tanstack/react-query";

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            retry: (failureCount, error) => {
                if (
                    typeof error === "object" &&
                    error !== null &&
                    "status" in error &&
                    typeof error.status === "number" &&
                    error.status >= 400 &&
                    error.status < 500 &&
                    error.status !== 429
                ) {
                    return false;
                }

                return failureCount < 3;
            },
        },
    },
});
