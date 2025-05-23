module.exports = {
  devServer: {
    port: 5174, // 前端端口
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端服务器地址
        changeOrigin: true, // 允许跨域
        logLevel: 'debug'
      }
    }
  }
}
