import { defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        projects: [
            {
                test: {
                    name: "unit",
                    environment: "node",
                    include: ["tests/**/*.test.ts"],
                    exclude: ["tests/**/*.integration.test.ts"],
                },
            },
            {
                test: {
                    name: "integration",
                    environment: "node",
                    include: ["tests/**/*.integration.test.ts"],
                    testTimeout: 35000,
                },
            },
        ],
    },
});
