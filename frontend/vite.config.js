import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// 构建信息：包含构建时间和版本号
const buildInfo = {
  version: process.env.npm_package_version || '1.0.0',
  buildTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
  buildTimestamp: Date.now()
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // 自动注入构建版本到 HTML
    {
      name: 'html-transform',
      transformIndexHtml(html) {
        return html
          .replace('<head>', `<head>\n  <meta name="app-version" content="${buildInfo.version}">\n  <meta name="build-time" content="${buildInfo.buildTime}">\n  <meta name="build-timestamp" content="${buildInfo.buildTimestamp}">`)
      }
    }
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  build: {
    // 确保 JS/CSS 文件名包含 hash（Vite 默认就会做）
    rollupOptions: {
      output: {
        // 确保每次构建都生成新的 hash
        entryFileNames: `assets/[name]-[hash].js`,
        chunkFileNames: `assets/[name]-[hash].js`,
        assetFileNames: `assets/[name]-[hash].[ext]`
      }
    }
  },
  // 定义构建时的全局变量
  define: {
    '__APP_VERSION__': JSON.stringify(buildInfo.version),
    '__BUILD_TIME__': JSON.stringify(buildInfo.buildTime),
    '__BUILD_TIMESTAMP__': JSON.stringify(buildInfo.buildTimestamp)
  }
})
