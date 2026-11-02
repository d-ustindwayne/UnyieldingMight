import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Relative base so the build works from GitHub Pages (/UnyieldingMight/) or any sub-path.
export default defineConfig({
  base: './',
  plugins: [react()],
});
