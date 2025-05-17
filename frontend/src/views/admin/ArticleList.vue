<template>
  <div class="article-list-container">
    <!-- 顶部操作栏 -->
    <div class="top-actions">
      <div class="search-box">
        <input 
          type="text" 
          placeholder="输入标题搜索" 
          v-model="searchKeyword"
          @keyup.enter="searchArticles" 
        />
        <!-- 调用接口: GET /api/admin/articles -->
        <button class="search-btn" @click="searchArticles">搜索</button>
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
            <th>账号</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(article, index) in filteredArticles" :key="index">
            <td>{{ article.title }}</td>
            <td>{{ article.content }}</td>
            <td>{{ article.status }}</td>
            <td>{{ article.author }}</td>
            <td class="actions-cell">
              <!-- 调用接口: POST /api/admin/articles/{id}/publish -->
              <button v-if="article.status === '未发布'" class="action-btn publish-btn">发布</button>
              <button class="action-btn edit-btn" v-if="article.status === '未发布'">编辑</button>
              <!-- 调用接口: DELETE /api/admin/articles/{id} -->
              <button class="action-btn delete-btn">删除</button>
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

// 模拟文章数据
// TODO: 实际项目中应通过API接口获取，使用 /api/admin/articles 接口
const articles = ref([
  {
    title: 'XXX',
    content: 'XXX',
    status: '已发布',
    author: '张三'
  },
  {
    title: 'XXX',
    content: 'XXX',
    status: '未发布',
    author: '赵管'
  }
])

// 使用计算属性实现前端搜索过滤
const filteredArticles = computed(() => {
  if (!searchKeyword.value) return articles.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return articles.value.filter(article => 
    article.title.toLowerCase().includes(keyword) ||
    article.content.toLowerCase().includes(keyword) ||
    article.author.toLowerCase().includes(keyword)
  )
})

// 搜索文章
// 调用接口: GET /api/admin/articles
// 参数说明: 
// - keyword: 搜索关键词
// - status: 文章状态 (可选)
// - author: 作者用户名 (可选)
function searchArticles() {
  // 前端已通过计算属性实现，这里只需用于表单提交时的处理
  // TODO: 实际项目中应该调用接口
  // 接口示例:
  // const params = {
  //   keyword: searchKeyword.value,
  //   page: 1,
  //   pageSize: 10
  // }
  // const { data } = await getArticles(params)
  // articles.value = data.list
  console.log('搜索关键字:', searchKeyword.value)
}

// 创建新博客
function createNewBlog() {
  alert('新建Blog功能将在后续完善')
}

// TODO: 添加以下功能和接口调用
// 1. 发布文章: POST /api/admin/articles/{id}/publish
// 2. al: POST /api/admin/articles/{id}/withdraw
// 3. 删除文章: DELETE /api/admin/articles/{id}
</script>

<style scoped>
.article-list-container {
  width: 100%;
}

.top-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  width: 400px;
  height: 40px; /* 添加固定高度 */
}

.search-box input {
  flex: 1;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px 0 0 4px;
  padding: 0 15px;
  font-size: 14px;
  border-right: none;
  box-sizing: border-box; /* 确保padding不会增加高度 */
}

.search-btn {
  width: 80px;
  height: 40px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 0 4px 4px 0;
  padding: 0 15px;
  cursor: pointer;
  font-size: 14px;
  box-sizing: border-box; /* 确保padding不会增加高度 */
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
  background-color: #eee;
  color: #333;
}

.delete-btn {
  background-color: #fff0f0;
  color: #ff4d4f;
}
</style>
