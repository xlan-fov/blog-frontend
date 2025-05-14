<template>
  <div class="content-management">
    <div class="content-header">
      <div class="search-box">
        <input
          v-model="searchKeyword"
          placeholder="请输入关键词搜索"
          class="search-input"
          @input="handleSearch"
        />
      </div>
      <button class="new-blog-btn" @click="createNewArticle">
        新建博客
      </button>
    </div>

    <table class="article-table">
      <thead>
        <tr>
          <th>内容标题</th>
          <th>正文</th>
          <th>内容状态</th>
          <th>修改时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="article in displayArticles" :key="article.id">
          <td>
            <router-link :to="`/blog/view/${article.id}`" class="blog-title-link">
              {{ article.title }}
            </router-link>
          </td>
          <td>
            <div class="content-preview">{{ article.content }}</div>
          </td>
          <td>
            <span :class="article.status === 'published' ? 'status-published' : 'status-draft'">
              {{ article.status === 'published' ? '已发布' : '未发布' }}
            </span>
          </td>
          <td>{{ article.updateTime }}</td>
          <td class="actions-cell">
            <button
              v-if="article.status !== 'published'"
              class="action-btn publish-btn"
              @click="publishArticle(article)"
            >
              发布
            </button>
            <button
              v-else
              class="action-btn withdraw-btn"
              @click="withdrawArticle(article)"
            >
              撤回
            </button>
            <button
              class="action-btn edit-btn"
              @click="editArticle(article)"
            >
              编辑
            </button>
            <button
              class="action-btn delete-btn"
              @click="deleteArticle(article)"
            >
              删除
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">上一页</button>
      <span>第 {{ currentPage }} 页，共 {{ totalPages }} 页</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')
const loading = ref(false)

// 模拟文章数据
const articles = ref([
  {
    id: '1',
    title: 'Vue3 组合式API最佳实践',
    content: 'Vue3的组合式API为我们提供了更灵活的代码组织方式，本文将详细介绍如何在实际项目中更好地使用组合式API...',
    status: 'published',
    updateTime: '2024-05-08 14:30:00'
  },
  {
    id: '2',
    title: 'Element Plus 组件库使用指南',
    content: 'Element Plus是一个基于Vue3的组件库，本文将从基础组件到高级组件，全面介绍其使用方法...',
    status: 'published',
    updateTime: '2024-05-08 13:20:00'
  },
  {
    id: '3',
    title: '前端性能优化实战',
    content: '本文将分享一些实用的前端性能优化技巧，包括代码分割、懒加载、缓存策略等...',
    status: 'draft',
    updateTime: '2024-05-08 12:15:00'
  },
  {
    id: '4',
    title: 'TypeScript 高级类型详解',
    content: '深入理解TypeScript的高级类型系统，包括泛型、条件类型、映射类型等概念...',
    status: 'published',
    updateTime: '2024-05-08 11:30:00'
  },
  {
    id: '5',
    title: 'Vite 构建工具配置指南',
    content: '详细介绍Vite的配置选项，以及如何根据项目需求进行优化...',
    status: 'draft',
    updateTime: '2024-05-08 10:45:00'
  }
])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

// 过滤后的文章
const filteredArticles = computed(() => {
  if (!searchKeyword.value) return articles.value
  
  return articles.value.filter(article => 
    article.title.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    article.content.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 总页数
const totalPages = computed(() => {
  return Math.ceil(filteredArticles.value.length / pageSize.value)
})

// 当前页显示的文章
const displayArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredArticles.value.slice(start, end)
})

// 上一页
const prevPage = () => {
  if (currentPage.value > 1) {
    currentPage.value--
  }
}

// 下一页
const nextPage = () => {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
  }
}

// 搜索文章
const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
}

// 新建文章
const createNewArticle = () => {
  router.push('/blog/create')
}

// 编辑文章
function editArticle(article) {
  router.push(`/blog/edit/${article.id}`)
}

// 发布文章
function publishArticle(article) {
  if (confirm(`确定要发布文章"${article.title}"吗？`)) {
    // 实际项目中应该调用API发布文章
    const index = articles.value.findIndex(item => item.id === article.id)
    if (index !== -1) {
      articles.value[index].status = 'published'
      articles.value[index].updateTime = new Date().toLocaleString()
    }
  }
}

// 撤回文章
function withdrawArticle(article) {
  if (confirm(`确定要撤回文章"${article.title}"吗？`)) {
    // 实际项目中应该调用API撤回文章
    const index = articles.value.findIndex(item => item.id === article.id)
    if (index !== -1) {
      articles.value[index].status = 'draft'
      articles.value[index].updateTime = new Date().toLocaleString()
    }
  }
}

// 删除文章
function deleteArticle(article) {
  if (confirm(`确定要删除文章"${article.title}"吗？`)) {
    // 实际项目中应该调用API删除文章
    const index = articles.value.findIndex(item => item.id === article.id)
    if (index !== -1) {
      articles.value.splice(index, 1)
    }
  }
}

onMounted(() => {
  // 这里可以调用API获取文章列表
  // 暂时使用模拟数据
})
</script>

<style scoped>
.content-management {
  padding: 20px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
}

.search-input {
  width: 300px;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 15px;
  font-size: 14px;
}

.new-blog-btn {
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 0 20px;
  height: 40px;
  cursor: pointer;
  font-size: 14px;
}

.new-blog-btn:hover {
  background-color: #4662d9;
}

.article-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.article-table th, .article-table td {
  padding: 12px 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.article-table th {
  background-color: #f8f9fa;
  font-weight: bold;
}

.content-preview {
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.blog-title-link {
  color: #4e6ef2;
  text-decoration: none;
}

.blog-title-link:hover {
  text-decoration: underline;
}

.status-published {
  color: #67c23a;
  background-color: #f0f9eb;
  padding: 2px 8px;
  border-radius: 4px;
}

.status-draft {
  color: #e6a23c;
  background-color: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
}

.actions-cell {
  display: flex;
  gap: 6px;
}

.action-btn {
  padding: 4px 8px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.publish-btn {
  background-color: #67c23a;
  color: white;
}

.withdraw-btn {
  background-color: #e6a23c;
  color: white;
}

.edit-btn {
  background-color: #409eff;
  color: white;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.pagination button {
  padding: 8px 15px;
  background-color: #f2f6fc;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media screen and (max-width: 768px) {
  .content-header {
    flex-direction: column;
    gap: 10px;
  }

  .search-box {
    width: 100%;
  }

  .search-input {
    width: 100%;
  }

  .article-table {
    display: block;
    overflow-x: auto;
  }

  .actions-cell {
    flex-wrap: wrap;
  }
}
</style>
