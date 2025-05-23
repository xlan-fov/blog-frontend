<template>
  <div class="admin-blog-view">
    <div class="page-header">
      <h2>查看博客</h2>
      <div class="header-actions">
        <el-button type="primary" @click="editBlog" v-if="canEdit">编辑</el-button>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>
    
    <div class="blog-content" v-loading="loading">
      <div class="blog-header">
        <h1 class="blog-title">{{ blog.title }}</h1>
        <div class="blog-meta">
          <span>作者：{{ blog.username }}</span>
          <span>状态：<el-tag :type="blog.status === 'published' ? 'success' : 'info'">
            {{ blog.status === 'published' ? '已发布' : '草稿' }}
          </el-tag></span>
          <span>创建时间：{{ formatDate(blog.createdAt) }}</span>
          <span v-if="blog.publishedAt">发布时间：{{ formatDate(blog.publishedAt) }}</span>
        </div>
      </div>
      
      <div class="blog-body" v-html="blog.content"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import blogApi from '@/api/blog'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const blog = ref({})
const loading = ref(false)

// 判断是否可以编辑（管理员可以编辑自己的博客）
const canEdit = computed(() => {
  return blog.value.username === userStore.userInfo.username
})

onMounted(async () => {
  await loadBlog()
})

const loadBlog = async () => {
  loading.value = true
  try {
    const res = await blogApi.getBlogById(route.params.id)
    if (res.code === 200 && res.data) {
      blog.value = res.data
    } else {
      ElMessage.error('博客不存在')
      goBack()
    }
  } catch (error) {
    console.error('获取博客失败:', error)
    // 添加更详细的错误信息
    if (error.response && error.response.status === 404) {
      ElMessage.error('博客不存在或已被删除')
    } else {
      ElMessage.error('获取博客失败')
    }
    goBack()
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/admin/blog-management')
}

const editBlog = () => {
  router.push(`/admin/blog-management/edit/${route.params.id}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}
</script>

<style scoped>
.admin-blog-view {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.blog-content {
  max-width: 1200px;
  margin: 0 auto;
}

.blog-header {
  margin-bottom: 32px;
}

.blog-title {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.blog-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  color: #666;
}

.blog-body {
  font-size: 16px;
  line-height: 1.8;
  color: #303133;
}

.blog-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 16px 0;
}

.blog-body :deep(pre) {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
}

.blog-body :deep(blockquote) {
  border-left: 4px solid #409EFF;
  padding-left: 16px;
  margin: 16px 0;
  color: #666;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .admin-blog-view {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .blog-title {
    font-size: 24px;
  }

  .blog-meta {
    flex-direction: column;
    gap: 8px;
  }

  .blog-body {
    font-size: 14px;
  }
}
</style>
