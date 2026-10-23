import { defineProject, mergeConfig } from "vitest/config";
import baseConfig from "../vitest.shared.ts";

export default mergeConfig(baseConfig, {
    test: {
        projects: [
            defineProject({
                test: {
                    name: "integration",
                    include: ["tests/**/*.integration.test.ts"],
                    environment: "node",
                    testTimeout: 35000,
                },
            }),
        ],
    },
});
