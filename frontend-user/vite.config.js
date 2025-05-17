import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  define: {
    // 注入环境变量到前端代码
    'import.meta.env.ADMIN_PATH': JSON.stringify('senti-admin-auth'), // 隐蔽的管理员路径
    'import.meta.env.APP_TITLE': JSON.stringify('SentiBlog')
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        ws: true,
        secure: false
      }
    }
  }
})
