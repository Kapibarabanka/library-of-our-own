import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
    plugins: [sveltekit()],
    server: {
        proxy: {
            '/api': {
                target: 'http://[::1]:8090',
                rewrite: path => path.replace(/^\/api/, ''),
                changeOrigin: true,
            },
        },
    },
    resolve: {
        alias: {
            '@app': path.resolve(__dirname, './src/app'),
        },
    },
});
