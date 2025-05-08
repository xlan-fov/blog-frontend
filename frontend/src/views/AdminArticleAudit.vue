<template>
  <div>
    <!-- 搜索+列表展示，支持下架/恢复操作 -->
    <!-- 这里的内容还没有完全实现 -->
  </div>
</template>
<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, onMounted } from 'vue'
// 导入管理员API函数
import { getBadContent } from '@/api/admin'

// 创建响应式的不良内容列表数据
const items = ref([])

// 组件挂载时自动加载不良内容列表
onMounted(async () => { 
  // 调用API获取不良内容列表
  items.value = (await getBadContent()).data 
})
</script>
