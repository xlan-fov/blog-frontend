// 这是应用的主入口文件，负责创建Vue应用实例并挂载到DOM

// 从Vue库导入createApp函数，用于创建应用实例
import { createApp } from 'vue'
// 导入全局CSS样式
import './style.css'
// 导入根组件App
import App from './App.vue'
// 导入前面定义的路由配置
import router from './router'
// 导入FontAwesome图标（保留这个因为可能已经在本地）

import '@fortawesome/fontawesome-free/css/all.min.css'

console.log('main.js 执行了')

// 创建Vue应用实例
const app = createApp(App)

// 使用路由
app.use(router)
// 挂载应用
app.mount('#app')
