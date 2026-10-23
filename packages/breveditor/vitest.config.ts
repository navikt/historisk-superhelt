import { mergeConfig } from "vitest/config";
import baseConfig from "../../vitest.shared.ts";

export default mergeConfig(baseConfig, {
    test: {
        setupFiles: ["./test/browser-setup.ts"],
    },
});
