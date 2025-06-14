<template>
  <div class="content-management">
    <div class="content-header">
      <el-input
        v-model="blogStore.searchKeyword"
        placeholder="请输入关键词搜索"
        class="search-input"
        clearable
        @clear="handleSearch"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        新建博客
      </el-button>
    </div>

    <el-table :data="displayBlogs" style="width: 100%" v-loading="loading">
      <el-table-column prop="title" label="内容标题" :min-width="isMobile ? 120 : 200">
        <template #default="{ row }">
          <router-link :to="`/dashboard/blog/view/${row.id}`" class="blog-title-link">
            {{ row.title }}
          </router-link>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="正文" min-width="300">
        <template #default="{ row }">
          <div class="content-preview">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="内容状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : 'warning'">
            {{ row.status === 'published' ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" :width="isMobile ? 80 : 240" fixed="right">
        <template #header>
          <div class="operation-header">操作</div>
        </template>
        <template #default="{ row }">
          <div class="operation-buttons">
            <el-button
              v-if="row.status !== 'published'"
              type="success"
              size="small"
              @click="handlePublish(row)"
            >
              发布
            </el-button>
            <el-button
              v-else
              type="warning"
              size="small"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalBlogs"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onBeforeUnmount, ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useBlogStore } from '@/stores/blog'
import { useUserStore } from '@/stores/user'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const blogStore = useBlogStore()
const userStore = useUserStore()

// 添加 loading 响应式变量
const loading = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalBlogs = computed(() => {
  const blogs = Array.isArray(blogStore.blogs) ? blogStore.blogs : []
  return blogs.length
})

// 搜索表单
const searchForm = reactive({
  title: '',
  status: ''
})

// 响应式判断是否为移动端
const isMobile = ref(window.innerWidth <= 768)
const handleResize = () => { isMobile.value = window.innerWidth <= 768 }

onMounted(() => { 
  window.addEventListener('resize', handleResize)
  // 确保博客数据被正确加载
  loadBlogsData()
})

onBeforeUnmount(() => { 
  window.removeEventListener('resize', handleResize) 
})

// 加载博客列表数据
const loadBlogsData = async () => {
  try {
    loading.value = true
    await blogStore.getBlogs({
      page: currentPage.value,
      pageSize: pageSize.value,
      title: searchForm.title,
      status: searchForm.status,
      userId: userStore.userInfo?.id
    })
    loading.value = false
  } catch (error) {
    console.error('加载博客列表失败:', error)
    ElMessage.error('获取博客列表失败')
    loading.value = false
  }
}

// 刷新列表数据
const loadBlogs = () => {
  loadBlogsData()
}

// 初始加载
onMounted(() => {
  loadBlogsData()
})

const handleSearch = () => {
  blogStore.searchBlogs(blogStore.searchKeyword)
  currentPage.value = 1 // 搜索时重置到第一页
}

const handleCreate = () => {
  router.push('/dashboard/blog/create')
}

const handleEdit = (row) => {
  router.push(`/dashboard/blog/edit/${row.id}`)
}

// 添加发布博客功能
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布这篇博客吗？', '确认发布', {
      confirmButtonText: '确定发布',
      cancelButtonText: '取消',
      type: 'info'
    })

    await blogStore.publishBlog({
      id: row.id,
      title: row.title,
      content: row.content
    })
    
    ElMessage.success('发布成功')
    loadBlogsData() // 使用正确的方法名刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleWithdraw = async (row) => {
  try {
    await ElMessageBox.confirm('确定要撤回这篇博客吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await blogStore.withdrawBlog(row.id)
    ElMessage.success('撤回成功')
    loadBlogsData() // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('撤回失败:', error)
      ElMessage.error('撤回失败: ' + (error.message || '未知错误'))
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇博客吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await blogStore.deleteBlog(row.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页处理函数
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1 // 重置到第一页
}

const handleCurrentChange = (page) => {
  currentPage.value = page
}

// 计算属性 - 修复数组检查和分页
const displayBlogs = computed(() => {
  // 确保 blogs 是数组
  const blogs = Array.isArray(blogStore.blogs) ? blogStore.blogs : []
  
  let filteredBlogs = blogs
  
  // 如果有搜索关键词，进行过滤
  if (blogStore.searchKeyword && blogStore.searchKeyword.trim() !== '') {
    const keyword = blogStore.searchKeyword.toLowerCase()
    filteredBlogs = blogs.filter(blog => {
      if (!blog) return false
      const title = blog.title ? blog.title.toLowerCase() : ''
      const content = blog.content ? blog.content.toLowerCase() : ''
      return title.includes(keyword) || content.includes(keyword)
    })
  }
  
  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredBlogs.slice(start, end)
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '暂无记录';
  
  try {
    const date = new Date(dateTime);
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) return dateTime;
    
    // 格式化为 YYYY-MM-DD HH:MM:SS
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).replace(/\//g, '-');
  } catch (error) {
    console.error('日期格式化错误:', error);
    return dateTime; // 出错时返回原始值
  }
}
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

.search-input {
  width: 300px;
}

.content-preview {
  max-height: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.blog-title-link {
  color: #409EFF;
  text-decoration: none;
  transition: color 0.3s;
}

.blog-title-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.operation-header {
  text-align: center;
  width: 100%;
}

.operation-buttons {
  display: flex;
  justify-content: center;
  align-items: stretch;
}

.operation-buttons .el-button {
  border-radius: 0;
  margin: 0;
  height: 32px;
  line-height: 32px;
  padding: 0 15px;
}

.operation-buttons .el-button:first-child {
  border-top-left-radius: 4px;
  border-bottom-left-radius: 4px;
}

.operation-buttons .el-button:last-child {
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

@media screen and (max-width: 768px) {
  .content-management {
    padding: 10px;
  }

  .content-header {
    flex-direction: column;
    gap: 10px;
  }

  .search-input {
    width: 100%;
  }

  .el-table {
    width: 100% !important;
    overflow-x: auto !important;
    -webkit-overflow-scrolling: touch !important;
  }

  .el-table::-webkit-scrollbar {
    width: 4px;
    height: 4px;
  }

  .el-table::-webkit-scrollbar-thumb {
    background: #909399;
    border-radius: 2px;
  }

  .el-table::-webkit-scrollbar-track {
    background: #f5f7fa;
  }

  .el-table__body-wrapper {
    overflow-x: auto !important;
    -webkit-overflow-scrolling: touch !important;
  }

  .operation-buttons {
    flex-direction: column;
    gap: 8px;
  }

  .operation-buttons .el-button {
    border-radius: 4px !important;
    height: 32px;
    line-height: 32px;
  }

  .pagination-wrapper {
    margin-top: 10px;
  }

  :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }
}
</style>