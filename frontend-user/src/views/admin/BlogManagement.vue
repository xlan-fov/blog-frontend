<template>
  <div class="blog-management">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="账号查询">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchUserBlogs">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-tabs v-model="activeTab" class="blog-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="我的博客" name="myBlogs">
        <div class="table-actions">
          <el-button type="primary" @click="showCreateDialog">新建博客</el-button>
        </div>
        
        <el-table :data="paginatedBlogs" border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'published' ? 'success' : 'info'">
                {{ scope.row.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="scope">
              <el-button type="primary" link @click="showBlogDetail(scope.row)">查看</el-button>
              <el-button type="primary" link @click="showEditDialog(scope.row)">编辑</el-button>
              <el-button type="danger" link @click="deleteBlog(scope.row)">删除</el-button>
              <el-button 
                v-if="scope.row.status !== 'published'" 
                type="success" 
                link 
                @click="publishBlog(scope.row)"
              >
                发布
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalMyBlogs"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
      
      <el-tab-pane label="用户博客" name="userBlogs">
        <el-table :data="paginatedUserBlogs" border style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="title" label="标题" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'published' ? 'success' : 'info'">
                {{ scope.row.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button type="primary" link @click="showBlogDetail(scope.row)">查看</el-button>
              <el-button type="danger" link @click="deleteBlog(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalUserBlogs"
            :page-size="pageSize"
            :current-page="currentPage"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 博客详情对话框 -->
    <el-dialog
      v-model="blogDetailDialogVisible"
      title="博客详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="detailLoading" class="blog-detail">
        <h2 class="blog-title">{{ currentBlog.title }}</h2>
        <div class="blog-meta">
          <span>作者：{{ currentBlog.username }}</span>
          <span>创建时间：{{ formatDateTime(currentBlog.createdAt || currentBlog.createTime) }}</span>
          <span>状态：{{ currentBlog.status === 'published' ? '已发布' : '草稿' }}</span>
        </div>
        <div class="blog-content" v-html="currentBlog.content"></div>
      </div>
    </el-dialog>
    
    <!-- 博客编辑对话框 -->
    <el-dialog
      v-model="blogEditDialogVisible"
      :title="isCreating ? '创建博客' : '编辑博客'"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-loading="editLoading" class="blog-edit">
        <el-form :model="blogForm" label-width="80px">
          <el-form-item label="标题" required>
            <el-input v-model="blogForm.title" placeholder="请输入博客标题"/>
          </el-form-item>
          <el-form-item label="内容" required>
            <el-input
              v-model="blogForm.content"
              type="textarea"
              :rows="10"
              placeholder="请输入博客内容"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="blogForm.status">
              <el-radio label="draft">草稿</el-radio>
              <el-radio label="published">发布</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="blogEditDialogVisible = false">取消</el-button>
          <el-button 
            type="success" 
            @click="exportBlogToPDF" 
            :disabled="!blogForm.id || isCreating"
            :loading="exportLoading"
          >导出PDF</el-button>
          <el-button type="primary" @click="saveBlog" :loading="saveLoading">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import adminApi from '@/api/admin'
import blogsApi from '@/api/blogs'

const router = useRouter()
const userStore = useUserStore()

// 搜索表单
const searchForm = ref({
  username: ''
})

// 表格数据
const myBlogs = ref([])
const userBlogs = ref([])
const totalMyBlogs = ref(0)
const totalUserBlogs = ref(0)
const loading = ref(false)
const activeTab = ref('myBlogs')
const backendAvailable = ref(true)  // 添加缺失的变量

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 计算当前需要显示的我的博客数据（添加分页计算）
const paginatedBlogs = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return myBlogs.value.slice(startIndex, endIndex);
});

// 计算当前需要显示的用户博客数据（添加分页计算）
const paginatedUserBlogs = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value;
  const endIndex = startIndex + pageSize.value;
  return userBlogs.value.slice(startIndex, endIndex);
});

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

// 检查后端连接状态
const checkBackendConnection = async () => {
  try {
    // 使用相对路径，通过代理访问后端
    await fetch('/api/health', { 
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
      // 设置较短的超时时间
      signal: AbortSignal.timeout(2000) 
    })
    backendAvailable.value = true
    return true
  } catch (error) {
    console.warn('后端服务器未连接:', error)
    backendAvailable.value = false
    ElMessage.warning({
      message: '后端服务未启动，显示模拟数据',
      duration: 5000,
      showClose: true
    })
    return false
  }
}

// 获取当前管理员的博客列表
const fetchMyBlogs = async () => {
  loading.value = true
  try {
    // 调用后端API获取当前管理员的博客
    const res = await adminApi.getBlogList() // 获取当前用户的博客
    
    if (res.code === 200 && res.data) {
      // 过滤出当前管理员的博客
      const currentUsername = userStore.userInfo.username
      const allBlogs = Array.isArray(res.data) ? res.data : res.data.list || []
      
      myBlogs.value = allBlogs.filter(blog => blog.username === currentUsername)
      totalMyBlogs.value = myBlogs.value.length
    } else {
      ElMessage.error(res.message || '获取博客列表失败')
      myBlogs.value = []
      totalMyBlogs.value = 0
    }
  } catch (error) {
    console.error('获取博客列表失败:', error)
    ElMessage.error('获取博客列表失败')
    myBlogs.value = []
    totalMyBlogs.value = 0
  } finally {
    loading.value = false
  }
}

// 搜索用户博客
const searchUserBlogs = async () => {
  loading.value = true
  try {
    // 调用后端API获取所有博客
    const res = await adminApi.getBlogList()
    
    if (res.code === 200 && res.data) {
      let allBlogs = Array.isArray(res.data) ? res.data : res.data.list || []
      
      // 如果有搜索用户名，进行过滤
      if (searchForm.value.username && searchForm.value.username.trim()) {
        allBlogs = allBlogs.filter(blog => 
          blog.username && blog.username.includes(searchForm.value.username.trim())
        )
      }
      
      userBlogs.value = allBlogs
      totalUserBlogs.value = allBlogs.length
      
      // 重置分页到第一页
      currentPage.value = 1
    } else {
      ElMessage.error(res.message || '获取用户博客列表失败')
      userBlogs.value = []
      totalUserBlogs.value = 0
    }
  } catch (error) {
    console.error('获取用户博客列表失败:', error)
    ElMessage.error('获取用户博客列表失败')
    userBlogs.value = []
    totalUserBlogs.value = 0
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value.username = ''
  searchUserBlogs()
}

// 创建博客
const showCreateDialog = () => {
  isCreating.value = true
  blogEditDialogVisible.value = true
  
  // 重置表单
  blogForm.value = {
    id: null,
    title: '',
    content: '',
    status: 'draft'
  }
}

// 查看博客详情
const blogDetailDialogVisible = ref(false)
const detailLoading = ref(false)
const currentBlog = ref({})

// 博客编辑对话框
const blogEditDialogVisible = ref(false)
const editLoading = ref(false)
const saveLoading = ref(false)
const exportLoading = ref(false)
const isCreating = ref(false)
const blogForm = ref({
  id: null,
  title: '',
  content: '',
  status: 'draft'
})

// 查看博客详情
const showBlogDetail = async (blog) => {
  detailLoading.value = true
  blogDetailDialogVisible.value = true
  currentBlog.value = { ...blog }
  
  try {
    // 调用API获取完整博客内容
    const res = await blogsApi.getBlogDetail(blog.id)
    
    if (res.code === 200 && res.data) {
      // 更新博客详情
      currentBlog.value = {
        ...currentBlog.value,
        ...res.data,
        content: res.data.content || currentBlog.value.content
      }
    } else {
      ElMessage.error(res.message || '获取博客详情失败')
    }
  } catch (error) {
    console.error('获取博客详情失败:', error)
    ElMessage.error('获取博客详情失败')
  } finally {
    detailLoading.value = false
  }
}

// 显示编辑对话框
const showEditDialog = async (blog) => {
  editLoading.value = true
  isCreating.value = false
  blogEditDialogVisible.value = true
  
  try {
    // 调用API获取完整博客内容用于编辑
    const res = await blogsApi.getBlogDetail(blog.id)
    
    if (res.code === 200 && res.data) {
      // 填充表单数据
      blogForm.value = {
        id: res.data.id,
        title: res.data.title || '',
        content: res.data.content || '',
        status: res.data.status || 'draft'
      }
    } else {
      ElMessage.error(res.message || '获取博客详情失败')
      blogEditDialogVisible.value = false
    }
  } catch (error) {
    console.error('获取博客详情失败:', error)
    ElMessage.error('获取博客详情失败')
    blogEditDialogVisible.value = false
  } finally {
    editLoading.value = false
  }
}

// 删除博客
const deleteBlog = (blog) => {
  ElMessageBox.confirm(`确定要删除博客 "${blog.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用后端API删除博客
      const res = await adminApi.deleteArticle(blog.id, { reason: '管理员删除' })
      
      if (res.code === 200) {
        ElMessage.success('删除成功')
        // 重新加载数据
        if (activeTab.value === 'myBlogs') {
          fetchMyBlogs()
        } else {
          searchUserBlogs()
        }
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 发布博客
const publishBlog = (blog) => {
  ElMessageBox.confirm(`确定要发布博客 "${blog.title}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      // 调用后端API发布博客
      const res = await adminApi.publishArticle(blog.id)
      
      if (res.code === 200) {
        ElMessage.success('发布成功')
        // 重新加载数据
        if (activeTab.value === 'myBlogs') {
          fetchMyBlogs()
        } else {
          searchUserBlogs()
        }
      } else {
        ElMessage.error(res.message || '发布失败')
      }
    } catch (error) {
      console.error('发布失败:', error)
      ElMessage.error('发布失败')
    }
  }).catch(() => {})
}

// 保存博客
const saveBlog = async () => {
  // 表单验证
  if (!blogForm.value.title.trim()) {
    ElMessage.warning('请输入博客标题')
    return
  }
  
  if (!blogForm.value.content.trim()) {
    ElMessage.warning('请输入博客内容')
    return
  }
  
  saveLoading.value = true
  try {
    let res
    
    if (isCreating.value) {
      // 创建新博客
      res = await blogsApi.createBlog({
        title: blogForm.value.title,
        content: blogForm.value.content,
        status: blogForm.value.status
      })
    } else {
      // 更新现有博客
      res = await blogsApi.updateBlog(blogForm.value.id, {
        title: blogForm.value.title,
        content: blogForm.value.content,
        status: blogForm.value.status
      })
    }
    
    if (res.code === 200) {
      ElMessage.success(isCreating.value ? '创建成功' : '更新成功')
      blogEditDialogVisible.value = false
      
      // 刷新数据
      if (activeTab.value === 'myBlogs') {
        fetchMyBlogs()
      } else {
        searchUserBlogs()
      }
    } else {
      ElMessage.error(res.message || (isCreating.value ? '创建失败' : '更新失败'))
    }
  } catch (error) {
    console.error(isCreating.value ? '创建博客失败:' : '更新博客失败:', error)
    ElMessage.error(isCreating.value ? '创建失败' : '更新失败')
  } finally {
    saveLoading.value = false
  }
}

// 导出博客为PDF
const exportBlogToPDF = async () => {
  if (!blogForm.value.id) {
    ElMessage.warning('请先保存博客后再导出PDF')
    return
  }
  
  exportLoading.value = true
  try {
    // 直接用fetch API调用后端接口，因为需要处理二进制文件
    const response = await fetch(`/api/export/${blogForm.value.id}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) {
      throw new Error(`导出失败: ${response.statusText}`)
    }
    
    // 获取文件流
    const blob = await response.blob()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.style.display = 'none'
    a.href = url
    
    // 从响应头获取文件名，如果没有则使用博客标题
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `${blogForm.value.title || 'blog'}.pdf`
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '')
      }
    }
    
    a.download = decodeURIComponent(filename)
    document.body.appendChild(a)
    a.click()
    
    // 清理
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage.error(`导出PDF失败: ${error.message || '未知错误'}`)
  } finally {
    exportLoading.value = false
  }
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  // 当页面大小改变时，重置为第一页
  currentPage.value = 1
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  // 无需重新获取数据，因为我们使用计算属性处理分页
}

// 监听标签页切换
const handleTabChange = (tab) => {
  // 切换标签页时重置分页
  currentPage.value = 1
  if (tab === 'myBlogs') {
    fetchMyBlogs()
  } else {
    searchUserBlogs()
  }
}

// 初始化
onMounted(() => {
  fetchMyBlogs()
  searchUserBlogs()
  checkBackendConnection()
})
</script>

<style scoped>
.blog-management {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.blog-tabs {
  background-color: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-actions {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.pagination {
  margin-top: 16px;
  text-align: right;
}

/* 添加表格居中样式 */
:deep(.el-table .cell) {
  text-align: center;
}

:deep(.el-table th) {
  text-align: center !important;
}

:deep(.el-table td) {
  vertical-align: middle;
}

/* 确保标签和按钮也居中 */
:deep(.el-table .el-tag) {
  display: inline-flex;
  justify-content: center;
  align-items: center;
}

:deep(.el-table .el-button-group),
:deep(.el-table .el-button) {
  display: inline-flex;
  justify-content: center;
}

/* 博客详情样式 */
.blog-detail {
  padding: 10px;
}

.blog-title {
  font-size: 24px;
  text-align: center;
  margin-bottom: 16px;
}

.blog-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  margin-bottom: 20px;
  border-bottom: 1px solid #EBEEF5;
  padding-bottom: 10px;
}

.blog-content {
  line-height: 1.8;
  max-height: 400px;
  overflow-y: auto;
}

/* 博客编辑样式 */
.blog-edit {
  padding: 10px;
}

/* 添加PDF按钮样式 */
:deep(.el-button--success) {
  margin-right: 10px;
}
</style>