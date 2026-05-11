/// <reference types="vitest/config" />

import path from "node:path";
import { tanstackRouter } from "@tanstack/router-plugin/vite";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        // Please make sure that '@tanstack/router-plugin' is passed before '@vitejs/plugin-react'
        tanstackRouter({
            target: "react",
            autoCodeSplitting: true,
        }),
        react(),
    ],
    resolve: {
        alias: {
            "~": path.resolve(__dirname, "src"),
            "@generated": path.resolve(__dirname, "generated"),
        },
    },
    test: {
        globals: true,
        environment: "jsdom",
    },
    define: {
        "process.env.COMMIT_SHA": JSON.stringify(process.env.COMMIT_SHA ?? "local"),
    },
    server: {
        // Proxy til wonderwall
        proxy: {
            "/api": "http://localhost:4000",
            "/oauth2": "http://localhost:4000",
        },
    },
});
