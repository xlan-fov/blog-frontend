<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="system-name" @click="goHome">
        SentiBlog
      </div>
      <div class="user-info">
        <el-dropdown v-if="userStore.isLoggedIn" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.userInfo.avatar" />
            <span class="username">{{ userStore.userInfo.username }}</span>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人空间</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-else @click="login">登录</el-button>
      </div>
    </div>

    <div class="main-content">
      <!-- PC端左侧菜单栏，移动端隐藏 -->
      <div v-if="!isMobile" class="left-menu">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          @select="handleSelect"
        >
          <el-menu-item index="blog">
            <el-icon><Document /></el-icon>
            <span>内容管理</span>
          </el-menu-item>
          <!-- 管理员菜单项可以在这里添加 -->
        </el-menu>
      </div>
      <!-- 移动端顶部下拉菜单 -->
      <div v-if="isMobile" class="mobile-menu">
        <el-select v-model="activeMenu" placeholder="选择菜单" @change="handleSelect" style="width: 100%">
          <el-option label="内容管理" value="blog" />
          <!-- 更多菜单项 -->
        </el-select>
      </div>
      <!-- 主要内容区域 -->
      <div class="content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('blog')

// 响应式判断是否是移动端
const isMobile = ref(window.innerWidth <= 768)
const handleResize = () => {
  isMobile.value = window.innerWidth <= 768
}
onMounted(() => {
  window.addEventListener('resize', handleResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

const goHome = () => {
  router.push('/')
}

const handleSelect = (key) => {
  if (key === 'blog') {
    router.push('/blog')
  }
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}

const login = () => {
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.top-nav {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.system-name {
  font-size: 20px;
  font-weight: bold;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  outline: none;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):hover) {
  background-color: #f5f7fa;
  color: #409EFF;
}

:deep(.el-dropdown-menu__item:not(.is-disabled):focus) {
  background-color: #f5f7fa;
  color: #409EFF;
}

.username {
  font-size: 14px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-menu {
  width: 200px;
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
}

/* 移动端顶部菜单样式 */
.mobile-menu {
  padding: 10px 20px;
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
}

/* 添加移动端布局切换 */
@media screen and (max-width: 768px) {
  .main-content {
    flex-direction: column !important;
    overflow: auto !important;
  }
  .content {
    padding: 10px !important;
  }
}
</style> 