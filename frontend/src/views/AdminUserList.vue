<template>
  <div>
    <!-- 表格展示所有用户，可踢蹬/封禁/解封 -->
    <!-- 这里的内容还没有完全实现 -->
  </div>
</template>
<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, onMounted } from 'vue'
// 导入管理员API函数
import { getUserList, kickUser, banUser, unbanUser } from '@/api/admin'

// 创建响应式的用户列表数据
const users = ref([])

// 组件挂载时自动加载用户列表
onMounted(async () => { 
  // 调用API获取用户列表
  users.value = (await getUserList()).data 
})
</script>
