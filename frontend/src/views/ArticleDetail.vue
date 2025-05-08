<template>
  <div class="article-detail">
    <!-- 文章标题 -->
    <h2>{{ article.title }}</h2>
    
    <!-- 文章内容，v-html指令用于渲染HTML内容 -->
    <div v-html="article.content"></div>
    
    <!-- 文章操作按钮区域 -->
    <div class="actions">
      <!-- 导出PDF按钮组件 -->
      <ExportPdfButton :id="article.id" />
      
      <!-- 编辑文章模态框组件 -->
      <EditArticleModal :article="article" @saved="refresh" />
      
      <!-- 删除确认对话框组件 -->
      <DeleteConfirmDialog :id="article.id" @deleted="goList" />
    </div>
  </div>
</template>
<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, onMounted } from 'vue'
// 导入Vue Router的组合式API函数
import { useRoute, useRouter } from 'vue-router'
// 导入文章API
import { fetchArticle } from '@/api/article'
// 导入自定义组件
import ExportPdfButton from '@/components/ExportPdfButton.vue'
import EditArticleModal from '@/components/EditArticleModal.vue'
import DeleteConfirmDialog from '@/components/DeleteConfirmDialog.vue'

// 获取当前路由对象，用于获取路由参数
const route  = useRoute()
// 获取路由器实例，用于页面导航
const router = useRouter()
// 创建响应式的文章数据对象
const article = ref({})

// 刷新文章数据的函数
async function refresh() {
  // 使用路由参数中的id获取文章详情
  const res = await fetchArticle(route.params.id)
  // 更新文章数据
  article.value = res.data
}

// 返回列表页的函数
function goList() {
  // 使用router实例进行编程式导航
  router.push({ name: 'ArticleList' })
}

// 组件挂载时自动加载文章详情
onMounted(refresh)
</script>