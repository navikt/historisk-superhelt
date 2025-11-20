// vite.config.ts
import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import {tanstackRouter} from '@tanstack/router-plugin/vite'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        // Please make sure that '@tanstack/router-plugin' is passed before '@vitejs/plugin-react'
        tanstackRouter({
            target: 'react',
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
    server: {
        // Proxy til wonderwall
        proxy: {
            '/api': 'http://localhost:4000',
            '/oauth2': 'http://localhost:4000'
        }
    }
})