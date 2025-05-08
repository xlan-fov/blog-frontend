<template>
  <div class="content-manage-container">
    <!-- 顶部操作栏 -->
    <div class="top-actions">
      <div class="search-box">
        <input type="text" placeholder="输入关键字搜索" v-model="searchKeyword" />
      </div>
      <button class="new-blog-btn" @click="createNewBlog">新建Blog</button>
    </div>
    
    <!-- 文章表格 -->
    <div class="article-table">
      <table>
        <thead>
          <tr>
            <th>内容标题</th>
            <th>正文</th>
            <th>内容状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(article, index) in filteredArticles" :key="index">
            <td>{{ article.title }}</td>
            <td>{{ article.content }}</td>
            <td>{{ article.status }}</td>
            <td class="actions-cell">
              <!-- 已发布的文章只能编辑和删除 -->
              <template v-if="article.status === '已发布'">
                <button class="action-btn edit-btn" @click="editArticle(article)">编辑</button>
                <button class="action-btn delete-btn" @click="deleteArticle(article)">删除</button>
              </template>
              
              <!-- 未发布的文章可以发布、编辑和删除 -->
              <template v-else-if="article.status === '未发布'">
                <button class="action-btn publish-btn" @click="publishArticle(article)">发布</button>
                <button class="action-btn edit-btn" @click="editArticle(article)">编辑</button>
                <button class="action-btn delete-btn" @click="deleteArticle(article)">删除</button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchKeyword = ref('')

// 文章数据
const articles = ref([
  {
    id: 1,
    title: 'XXX',
    content: 'XXX',
    status: '已发布',
    author: '张三'
  },
  {
    id: 2,
    title: 'XXX',
    content: 'XXX',
    status: '未发布',
    author: '张三'
  }
])

// 过滤后的文章列表
const filteredArticles = computed(() => {
  if (!searchKeyword.value) {
    return articles.value
  }
  
  return articles.value.filter(article => 
    article.title.includes(searchKeyword.value) ||
    article.content.includes(searchKeyword.value)
  )
})

// 新建博客
function createNewBlog() {
  router.push({ name: 'BlogEditor', params: { id: 'new' } })
}

// 发布文章
function publishArticle(article) {
  // 实际项目中应该调用API发布文章
  article.status = '已发布'
}

// 编辑文章
function editArticle(article) {
  router.push({ name: 'BlogEditor', params: { id: article.id } })
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
</script>

<style scoped>
.content-manage-container {
  width: 100%;
}

.top-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box input {
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

.article-table {
  background-color: white;
  border-radius: 4px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

th {
  font-weight: normal;
  color: #333;
  background-color: #fafafa;
}

.actions-cell {
  display: flex;
  gap: 10px;
}

.action-btn {
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

.publish-btn {
  background-color: #4e6ef2;
  color: white;
}

.edit-btn {
  background-color: #f0f0f0;
  color: #333;
}

.delete-btn {
  background-color: #fff0f0;
  color: #ff4d4f;
}
</style>
