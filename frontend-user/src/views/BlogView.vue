<template>
  <div class="blog-view">
    <div class="blog-container" v-loading="loading">
      <div v-if="blog" class="blog-content">
        <h1 class="blog-title">{{ blog.title }}</h1>
        <div class="blog-meta">
          <span class="update-time">更新时间：{{ blog.updatedAt }}</span>
          <el-tag :type="blog.status === 'published' ? 'success' : 'warning'" class="status-tag">
            {{ blog.status === 'published' ? '已发布' : '未发布' }}
          </el-tag>
        </div>
        <div class="blog-body" v-html="blog.content"></div>
      </div>
      <div v-else class="no-content">
        <el-empty description="博客内容不存在" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogStore } from '@/stores/blog'
import { ElMessage } from 'element-plus'

const route = useRoute()
const blogStore = useBlogStore()
const loading = ref(false)
const blog = ref(null)

const fetchBlog = async () => {
  loading.value = true
  try {
    const blogId = route.params.id
    blog.value = await blogStore.getBlogById(blogId)
  } catch (error) {
    console.error('获取博客失败:', error)
    ElMessage.error('获取博客详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchBlog()
})
</script>

<style scoped>
.blog-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.blog-container {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.blog-title {
  font-size: 2em;
  color: #303133;
  margin-bottom: 16px;
}

.blog-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  color: #909399;
}

.status-tag {
  margin-left: 8px;
}

.blog-body {
  line-height: 1.8;
  color: #606266;
}

.blog-body :deep(h1),
.blog-body :deep(h2),
.blog-body :deep(h3),
.blog-body :deep(h4),
.blog-body :deep(h5),
.blog-body :deep(h6) {
  margin: 16px 0;
  color: #303133;
}

.blog-body :deep(p) {
  margin: 12px 0;
}

.blog-body :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
}

.blog-body :deep(blockquote) {
  margin: 16px 0;
  padding: 8px 16px;
  border-left: 4px solid #409EFF;
  background: #f5f7fa;
}

@media screen and (max-width: 768px) {
  .blog-view {
    padding: 10px;
  }

  .blog-container {
    padding: 16px;
  }

  .blog-title {
    font-size: 1.5em;
  }

  .blog-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>