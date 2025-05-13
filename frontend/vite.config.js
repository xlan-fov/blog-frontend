// 这是Vite构建工具的配置文件，用于设置前端项目的构建选项

// 引入Vite的defineConfig函数，它用于类型提示和配置验证
import { defineConfig } from 'vite'
// 引入Vue插件，使Vite能够处理.vue文件
import vue from '@vitejs/plugin-vue'
// 引入Node.js的path模块，用于处理文件路径
import path from 'path'

// 导出配置对象
export default defineConfig({
  // 设置基础公共路径为当前目录('./')，这样构建后的资源会使用相对路径
  base: './',
  // 配置插件，这里使用vue()插件处理Vue单文件组件
  plugins: [vue()],
  // 解析配置
  resolve: {
    // 路径别名配置，让'@'符号指向src目录，方便引入文件
    alias: {
      '@': path.resolve(__dirname, 'src'),  // __dirname表示当前文件所在目录
    }
  },
  // 开发服务器配置
  server: {
    // 配置历史模式回退，确保路由在开发环境正常工作
    historyApiFallback: true
  }
})
