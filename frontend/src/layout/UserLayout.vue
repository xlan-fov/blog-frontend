<template>
  <div class="user-container">
    <!-- 左侧导航栏 -->
    <div class="sidebar">
      <div class="logo">XXX 管理系统</div>
      <div class="nav-menu">
        <div class="nav-item active" @click="navigateTo('UserContentManage')">
          内容管理
        </div>
      </div>
    </div>
    
    <!-- 右侧内容区域 -->
    <div class="main-content">
      <!-- 顶部信息栏 -->
      <div class="header">
        <div class="user-info">
          <span>当前账号信息</span>
          <button class="logout-btn" @click="logout">登出</button>
        </div>
      </div>
      
      <!-- 主内容区域 -->
      <div class="content-wrapper">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 页面导航
function navigateTo(routeName) {
  router.push({ name: routeName })
}

// 退出登录
function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('userRole')
  router.push({ name: 'Login' })
}
</script>

<style scoped>
.user-container {
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
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  padding-left: 20px;
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
}

.nav-item.active {
  color: #333;
  font-weight: bold;
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
  justify-content: flex-end;
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

.content-wrapper {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}
</style>
