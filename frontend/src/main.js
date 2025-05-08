// 这是应用的主入口文件，负责创建Vue应用实例并挂载到DOM

// 从Vue库导入createApp函数，用于创建应用实例
import { createApp } from 'vue'
// 导入全局CSS样式
import './style.css'
// 导入根组件App
import App from './App.vue'
// 导入前面定义的路由配置
import router from './router'

console.log('main.js 执行了')

// 创建Vue应用实例
createApp(App).use(router).mount('#app')
