import { playwright } from "@vitest/browser-playwright";
import { configDefaults, defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        reporters: ["default"],
        environment: "node",
        projects: [
            {
                extends: true,
                test: {
                    name: "unit",
                    browser: {
                        enabled: false,
                    },
                    include: ["**/*.test.{ts,tsx}"],
                    exclude: [
                        ...configDefaults.exclude,
                        "**/*.browser.test.{ts,tsx}",
                        "**/*.integration.test.{ts,tsx}",
                    ],
                },
            },
            {
                extends: true,
                test: {
                    name: "browser",
                    include: ["**/*.browser.test.{ts,tsx}"],
                    browser: {
                        enabled: true,
                        provider: playwright(),
                        instances: [{ browser: "chromium" }],
                        headless: true,
                    },
                },
            },
        ],
    },
});
