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
          <el-button type="primary" @click="createBlog">新建博客</el-button>
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
              <el-button type="primary" link @click="viewBlog(scope.row)">查看</el-button>
              <el-button type="primary" link @click="editBlog(scope.row)">编辑</el-button>
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
              <el-button type="primary" link @click="viewBlog(scope.row)">查看</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import adminApi from '@/api/admin'

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
const createBlog = () => {
  router.push('/admin/blog-management/create')
}

// 查看博客
const viewBlog = (blog) => {
  router.push(`/admin/blog-management/view/${blog.id}`)
}

// 编辑博客
const editBlog = (blog) => {
  router.push(`/admin/blog-management/edit/${blog.id}`)
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
</style>