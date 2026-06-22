import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    host: '0.0.0.0',      // 允许局域网访问
    port: 5173,           // 前端端口（默认）
    proxy: {
      '/api': {           // 所有以 /api 开头的请求
        target: 'http://localhost:8080', // 后端地址
        changeOrigin: true,
        // 如果后端接口路径不带 /api 前缀，可取消下面注释
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})