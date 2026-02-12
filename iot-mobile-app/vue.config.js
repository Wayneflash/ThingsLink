module.exports = {
  devServer: {
    proxy: {
      '/api': {
        // 后端默认 8081，避免与 Clash 等占用 8080 的软件冲突
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  }
}
