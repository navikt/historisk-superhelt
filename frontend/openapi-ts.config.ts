import {defaultPlugins, defineConfig} from '@hey-api/openapi-ts';

export default defineConfig({
    input: 'http://localhost:8080/v3/api-docs',
    output: 'src/gen',
    plugins: [
        ...defaultPlugins,
        {
            name: '@hey-api/client-fetch',
            baseUrl: '',
        },
        '@tanstack/react-query',
    ],
});