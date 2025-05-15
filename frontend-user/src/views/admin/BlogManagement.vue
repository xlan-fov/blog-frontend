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
    
    <el-tabs v-model="activeTab" class="blog-tabs">
      <el-tab-pane label="我的博客" name="myBlogs">
        <div class="table-actions">
          <el-button type="primary" @click="createBlog">新建博客</el-button>
        </div>
        
        <el-table :data="myBlogs" border style="width: 100%" v-loading="loading">
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
        <el-table :data="userBlogs" border style="width: 100%" v-loading="loading">
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

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

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

// 获取当前管理员的博客列表
const fetchMyBlogs = async () => {
  loading.value = true
  try {
    // TODO: 替换为axios请求
    // 模拟数据
    setTimeout(() => {
      myBlogs.value = Array(15).fill().map((_, index) => ({
        id: index + 1,
        title: `管理员博客标题 ${index + 1}`,
        content: '这是博客内容...',
        category: '管理公告',
        createTime: new Date().toLocaleString(),
        status: index % 3 === 0 ? 'draft' : 'published'
      }))
      totalMyBlogs.value = myBlogs.value.length
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取博客列表失败')
    loading.value = false
  }
}

// 搜索用户博客
const searchUserBlogs = async () => {
  loading.value = true
  try {
    // TODO: 替换为axios请求
    // 模拟数据
    setTimeout(() => {
      userBlogs.value = Array(20).fill().map((_, index) => ({
        id: index + 1,
        username: searchForm.value.username || `user${index + 1}`,
        title: `用户博客标题 ${index + 1}`,
        content: '这是博客内容...',
        category: '生活随笔',
        createTime: new Date().toLocaleString(),
        status: index % 4 === 0 ? 'draft' : 'published'
      }))
      totalUserBlogs.value = userBlogs.value.length
      loading.value = false
    }, 500)
  } catch (error) {
    ElMessage.error('获取用户博客列表失败')
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
      // TODO: 替换为axios请求
      // 模拟删除
      if (activeTab.value === 'myBlogs') {
        myBlogs.value = myBlogs.value.filter(item => item.id !== blog.id)
        totalMyBlogs.value--
      } else {
        userBlogs.value = userBlogs.value.filter(item => item.id !== blog.id)
        totalUserBlogs.value--
      }
      ElMessage.success('删除成功')
    } catch (error) {
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
      // TODO: 替换为axios请求
      // 模拟发布
      blog.status = 'published'
      ElMessage.success('发布成功')
    } catch (error) {
      ElMessage.error('发布失败')
    }
  }).catch(() => {})
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  if (activeTab.value === 'myBlogs') {
    fetchMyBlogs()
  } else {
    searchUserBlogs()
  }
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  if (activeTab.value === 'myBlogs') {
    fetchMyBlogs()
  } else {
    searchUserBlogs()
  }
}

// 初始化
onMounted(() => {
  fetchMyBlogs()
  searchUserBlogs()
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