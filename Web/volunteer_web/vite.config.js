import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { fileURLToPath } from 'url'

// ES module 中无法直接使用 __dirname，需要手动定义
const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '') // 取决于后端是否带 /api, 你的 application.yml 配置了 context-path: /api
        // 你的后端配置 context-path: /api，所以前端请求 /api/auth/login -> 后端 /api/auth/login，不需要 rewrite
      }
    }
  }
})
