import { mergeConfig } from "vitest/config";
import baseConfig from "../vitest.shared.ts";
import viteConfig from "./vite.config.ts";

export default mergeConfig(mergeConfig(viteConfig, baseConfig), {
    test: {
        globals: true,
        environment: "jsdom",
    },
});
