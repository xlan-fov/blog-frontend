<template>
  <div class="admin-layout">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <div class="system-name">
        SentiBlog 管理系统
      </div>
      <div class="user-info">
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-dropdown">
            <el-avatar :size="32" :src="userStore.userInfo.avatar" />
            <span class="username">{{ userStore.userInfo.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="switch">切换到普通视图</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <div class="main-content">
      <!-- PC端左侧菜单栏，移动端隐藏 -->
      <div v-if="!isMobile" class="left-menu">
        <el-menu
          :default-active="activeIndex"
          router
          class="el-menu-vertical"
        >
          <el-menu-item index="/admin/blog-management">
            <el-icon><Document /></el-icon>
            <span>博客内容管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/account-management">
            <el-icon><User /></el-icon>
            <span>账号管理</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/anomaly-detection">
            <el-icon><Warning /></el-icon>
            <span>异常感知</span>
          </el-menu-item>
          
          <el-menu-item index="/admin/profile">
            <el-icon><Avatar /></el-icon>
            <span>个人空间</span>
          </el-menu-item>
        </el-menu>
      </div>
      
      <!-- 移动端顶部下拉菜单 -->
      <div v-if="isMobile" class="mobile-menu">
        <el-select v-model="activeRoute" placeholder="选择菜单" @change="handleSelect" style="width: 100%">
          <el-option label="博客内容管理" value="/admin/blog-management" />
          <el-option label="账号管理" value="/admin/account-management" />
          <el-option label="异常感知" value="/admin/anomaly-detection" />
          <el-option label="个人空间" value="/admin/profile" />
        </el-select>
      </div>
      
      <!-- 内容区域 -->
      <div class="content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Document, User, Warning, Avatar, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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

// 计算当前激活的菜单项
const activeIndex = computed(() => route.path)
const activeRoute = computed({
  get: () => route.path,
  set: (value) => value
})

// 处理移动端菜单选择
const handleSelect = (path) => {
  router.push(path)
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/admin-login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/admin/profile')
  } else if (command === 'switch') {
    router.push('/')
  }
}
</script>

<style scoped>
.admin-layout {
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
  color: #409EFF;
}

.user-info {
  display: flex;
  align-items: center;
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
  background-color: #f5f7fa;
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

/* 移动端布局切换 */
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