<template>
  <div class="app-container">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="logo">XXX 管理系统</div>
      <div class="nav-menu">
        <div class="nav-item" :class="{ active: currentRoute === 'content' }" @click="navigateTo('ArticleList')">
          内容管理
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'account' }" @click="navigateTo('AccountManage')">
          账号管理
        </div>
        <div class="nav-item" :class="{ active: currentRoute === 'anomaly' }" @click="navigateTo('AnomalyDetection')">
          异常感知
        </div>
      </div>
    </div>
    
    <!-- 右侧主内容区 -->
    <div class="main-content">
      <!-- 顶部用户信息和登出按钮 -->
      <div class="header">
        <div class="search-wrapper">
          <!-- 此处为占位，实际内容在具体页面中 -->
        </div>
        <div class="user-info">
          <span>当前账号信息</span>
          <button class="logout-btn" @click="logout">登出</button>
        </div>
      </div>
      
      <!-- 实际内容区域 -->
      <div class="content-wrapper">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 当前选中的路由
const currentRoute = computed(() => {
  const path = route.path
  if (path.includes('article')) return 'content'
  if (path.includes('account')) return 'account'
  if (path.includes('anomaly')) return 'anomaly'
  return ''
})

// 页面导航
function navigateTo(routeName) {
  router.push({ name: routeName })
}

// 退出登录
function logout() {
  localStorage.removeItem('token')
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
}

.sidebar {
  width: 200px;
  background-color: #fff;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  border-bottom: 1px solid #eee;
}

.nav-menu {
  padding-top: 20px;
}

.nav-item {
  padding: 15px 20px;
  cursor: pointer;
  font-size: 16px;
}

.nav-item:hover {
  background-color: #f5f7fa;
}

.nav-item.active {
  color: #4e6ef2;
  font-weight: bold;
  background-color: #f0f2ff;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.header {
  height: 60px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid #eee;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logout-btn {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 14px;
}

.logout-btn:hover {
  color: #4e6ef2;
}

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
