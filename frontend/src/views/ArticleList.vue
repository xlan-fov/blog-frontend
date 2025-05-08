<template>
  <div class="dashboard-layout">
    <!-- 左侧边栏 -->
    <div class="sidebar">
      <h2 class="system-title">XXX 管理系统</h2>
      <div class="menu-item active">内容管理</div>
    </div>
    
    <!-- 右侧内容区 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="top-nav">
        <div></div>
        <div class="user-actions">
          <span class="account-info">当前账号信息</span>
          <a @click="logout" class="logout-btn">登出</a>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="content-area">
        <!-- 搜索和新建区域 -->
        <div class="action-row">
          <div class="search-wrap">
            <SearchInput
              v-model="search"
              @search="loadArticles"
              placeholder="输入关键字搜索"
            />
          </div>
          <button class="new-blog-btn" @click="goToEditor">新建Blog</button>
        </div>
        
        <!-- 文章表格 -->
        <table class="article-table">
          <thead>
            <tr>
              <th>内容标题</th>
              <th>正文</th>
              <th>内容状态</th>
              <th>账号</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in articles" :key="item.id">
              <td>{{ item.title }}</td>
              <td>{{ item.content && item.content.substring(0, 20) + '...' }}</td>
              <td>{{ item.status === 'published' ? '已发布' : '未发布' }}</td>
              <td>{{ item.author }}</td>
              <td class="operations">
                <button class="op-btn edit-btn">编辑</button>
                <button v-if="item.status !== 'published'" class="op-btn publish-btn">发布</button>
                <button class="op-btn delete-btn">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- 分页组件 -->
        <Pagination
          :current-page="page"
          :total="total"
          @change="onPageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 导入文章API
import { fetchArticles } from '@/api/article'
import { logout as logoutApi } from '@/api/user'
// 导入自定义组件
import SearchInput from '@/components/SearchInput.vue'
import Pagination from '@/components/Pagination.vue'

const router = useRouter()
// 创建响应式的文章列表数据
const articles = ref([])
// 文章总数
const total    = ref(0)
// 当前页码
const page     = ref(1)
// 搜索关键词
const search   = ref('')

// 加载文章列表的函数
async function loadArticles() {
  try {
    // 调用API获取文章列表数据，传入当前页码和搜索关键词
    const res = await fetchArticles({ page: page.value, search: search.value })
    // 更新文章列表和总数
    articles.value = res.data.results
    total.value    = res.data.count
  } catch (error) {
    console.error('获取文章列表失败', error)
  }
}

// 页码变化处理函数
function onPageChange(p) {
  page.value = p
  loadArticles()
}

// 跳转到编辑页
function goToEditor() {
  router.push('/editor')
}

// 登出处理
async function logout() {
  try {
    await logoutApi()
    localStorage.removeItem('token')
    router.push('/login')
  } catch (error) {
    console.error('登出失败', error)
  }
}

// 组件挂载时自动加载文章列表
onMounted(loadArticles)
</script>

<style scoped>
.dashboard-layout {
  display: flex;
  min-height: 100vh;
  background-color: #f6f6f6;
}

/* 侧边栏样式 */
.sidebar {
  width: 210px;
  background: white;
  border-right: 1px solid #e6e6e6;
  padding-top: 20px;
  box-shadow: 2px 0 5px rgba(0,0,0,0.05);
}

.system-title {
  padding: 0 20px;
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 30px;
}

.menu-item {
  padding: 12px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.menu-item:hover, .menu-item.active {
  background-color: #f0f0f0;
}

/* 主内容区样式 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏样式 */
.top-nav {
  height: 50px;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.account-info {
  color: #666;
}

.logout-btn {
  color: #3385ff;
  cursor: pointer;
}

/* 内容区域样式 */
.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 搜索和新建区域样式 */
.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-wrap {
  width: 340px;
}

.new-blog-btn {
  background: #3385ff;
  color: white;
  border: none;
  padding: 0 20px;
  height: 40px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.new-blog-btn:hover {
  background: #2672ff;
}

/* 表格样式 */
.article-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  margin-bottom: 20px;
}

.article-table th, .article-table td {
  padding: 12px 15px;
  text-align: left;
  border: 1px solid #e6e6e6;
}

.article-table th {
  background: #f7f7f7;
  font-weight: 500;
}

.operations {
  white-space: nowrap;
}

.op-btn {
  padding: 4px 8px;
  margin-right: 5px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.edit-btn {
  background-color: #f0f0f0;
  color: #333;
}

.publish-btn {
  background-color: #67c23a;
  color: white;
}

.delete-btn {
  background-color: #f56c6c;
  color: white;
}
</style>