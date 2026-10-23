import { playwright } from "@vitest/browser-playwright";
import { defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        browser: {
            enabled: true,
            provider: playwright(),
            instances: [{ browser: "chromium" }],
            headless: true,
        },
        projects: [
            {
                extends: true,
                test: {
                    name: "unit",
                    environment: "node",
                    browser: {
                        enabled: false,
                    },
                    include: ["src/**/*.test.ts"],
                    exclude: ["src/**/*.browser.test.ts"],
                },
            },
            {
                extends: true,
                test: {
                    name: "browser",
                    include: ["src/**/*.browser.test.{ts,tsx}"],
                    setupFiles: ["./test/browser-setup.ts"],
                },
            },
        ],
    },
});
