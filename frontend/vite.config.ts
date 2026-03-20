import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  base: '/',
  build: {
    // Relative to frontend/ (config file directory)
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true,
  },
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // Dev: browser calls same origin; Vite forwards /api to Spring Boot
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
