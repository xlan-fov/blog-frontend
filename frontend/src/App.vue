<script setup>
// 导入Vue生命周期钩子
import { onMounted } from 'vue'

// 组件挂载时执行的函数
onMounted(() => {
  console.log('App mounted')
})
</script>

<template>
  <div class="app-container">
    <!-- 移除全局顶部导航栏 -->
    
    <!-- 主内容区域 -->
    <main>
      <!-- router-view是路由视图组件，显示当前路由匹配的组件 -->
      <!-- v-slot获取当前要渲染的组件 -->
      <router-view v-slot="{ Component }">
        <template v-if="Component">
          <!-- keep-alive可以缓存组件状态，避免重复创建 -->
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </template>
        <template v-else>
          <div>加载中...</div>
        </template>
      </router-view>
    </main>
  </div>
</template>

<style scoped>
/* Logo样式 */
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>

<style>
/* 应用容器样式 */
.app-container {
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
}

/* 移除固定导航相关样式 */
/* 
header {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 50px;
  background: var(--bg-color);
  border-bottom: none;
  box-shadow: none;
  z-index: 1000;
}
*/

/* 移除主内容的顶部边距 */
.app-container main {
  margin-top: 0;
}
</style>
