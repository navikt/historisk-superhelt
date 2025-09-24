import {defaultPlugins, defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'http://localhost:8080/v3/api-docs',
    output: 'src/api',
    plugins: [
        ...defaultPlugins,
        {
            name: '@hey-api/client-fetch',
            baseUrl: '',
        },
        '@tanstack/react-query',
    ],
});