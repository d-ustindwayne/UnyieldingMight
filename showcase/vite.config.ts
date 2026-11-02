import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Absolute base: the site is served from the root of a custom domain, and GitHub Pages
// shows 404.html at whatever path was requested, so relative asset URLs would break there.
// Inputs are relative to showcase/, where every script (and CI) runs the build.
export default defineConfig({
  base: '/',
  plugins: [react()],
  build: {
    rollupOptions: {
      input: { main: 'index.html', notFound: '404.html' },
    },
  },
});
