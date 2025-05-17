module.exports = {
  devServer: {
    port: 8080, // 前端端口
    proxy: {
      '/api': {
        target: 'http://localhost:8081', // 后端服务器地址
        changeOrigin: true, // 允许跨域
        ws: true, // 允许websocket
        secure: false, // 如果是https接口，需要配置这个参数
        pathRewrite: {
          '^/api': '/api' // 路径重写，这里可以根据实际情况配置
        }
      }
    }
  }
}
