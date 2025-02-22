import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
    plugins: [sveltekit(), tailwindcss()],
    server: {
        proxy: {
            '/api': {
                target: 'http://[::1]:8090',
                rewrite: path => path.replace(/^\/api/, ''),
                changeOrigin: true,
            },
        },
    },
});
