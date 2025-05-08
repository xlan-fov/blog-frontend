<template>
  <div class="blog-detail-container">
    <div class="blog-header">
      <h1 class="blog-title">{{ blog.title }}</h1>
      <div class="blog-meta">
        <span class="blog-author">作者：{{ blog.author }}</span>
        <span class="blog-date">发布时间：{{ blog.date }}</span>
      </div>
    </div>
    
    <div class="blog-content">
      <p v-for="(paragraph, index) in blog.content" :key="index">
        {{ paragraph }}
      </p>
    </div>
    
    <div class="blog-actions">
      <button class="action-btn" @click="goBack">返回列表</button>
      <button class="action-btn like-btn" @click="toggleLike">
        {{ isLiked ? '已点赞' : '点赞' }} ({{ blog.likes }})
      </button>
      <button class="action-btn favorite-btn" @click="toggleFavorite">
        {{ isFavorited ? '已收藏' : '收藏' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const blogId = route.params.id

// 状态变量
const isLiked = ref(false)
const isFavorited = ref(false)

// 博客数据（实际应从API获取）
const blog = ref({
  id: blogId,
  title: '博客文章详情',
  author: '张三',
  date: '2023-11-01',
  likes: 42,
  content: [
    '这是博客文章的第一段内容。这里展示了文章的详细内容，用户可以阅读完整的博客文章。',
    '这是博客文章的第二段内容。在实际应用中，这些内容将从后端API获取。',
    '这是博客文章的第三段内容。您可以通过点击底部的按钮对文章进行点赞或收藏操作。'
  ]
})

// 获取博客详情
onMounted(() => {
  // 实际项目中应该调用API获取博客详情
  // fetchBlogDetail(blogId)
  
  // 示例：检查是否已点赞/收藏（实际应从API或本地存储获取）
  const likedBlogs = JSON.parse(localStorage.getItem('likedBlogs') || '[]')
  isLiked.value = likedBlogs.includes(Number(blogId))
  
  const favoritedBlogs = JSON.parse(localStorage.getItem('favoritedBlogs') || '[]')
  isFavorited.value = favoritedBlogs.includes(Number(blogId))
})

// 返回列表
function goBack() {
  router.push({ name: 'UserHome' })
}

// 点赞操作
function toggleLike() {
  // 实际项目中应调用API处理点赞
  isLiked.value = !isLiked.value
  
  if (isLiked.value) {
    blog.value.likes++
    // 存储点赞状态
    const likedBlogs = JSON.parse(localStorage.getItem('likedBlogs') || '[]')
    if (!likedBlogs.includes(Number(blogId))) {
      likedBlogs.push(Number(blogId))
      localStorage.setItem('likedBlogs', JSON.stringify(likedBlogs))
    }
  } else {
    blog.value.likes--
    // 移除点赞状态
    const likedBlogs = JSON.parse(localStorage.getItem('likedBlogs') || '[]')
    const index = likedBlogs.indexOf(Number(blogId))
    if (index > -1) {
      likedBlogs.splice(index, 1)
      localStorage.setItem('likedBlogs', JSON.stringify(likedBlogs))
    }
  }
}

// 收藏操作
function toggleFavorite() {
  // 实际项目中应调用API处理收藏
  isFavorited.value = !isFavorited.value
  
  // 存储收藏状态
  const favoritedBlogs = JSON.parse(localStorage.getItem('favoritedBlogs') || '[]')
  
  if (isFavorited.value) {
    if (!favoritedBlogs.includes(Number(blogId))) {
      favoritedBlogs.push(Number(blogId))
    }
  } else {
    const index = favoritedBlogs.indexOf(Number(blogId))
    if (index > -1) {
      favoritedBlogs.splice(index, 1)
    }
  }
  
  localStorage.setItem('favoritedBlogs', JSON.stringify(favoritedBlogs))
}
</script>

<style scoped>
.blog-detail-container {
  max-width: 800px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.blog-header {
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}

.blog-title {
  font-size: 24px;
  color: #333;
  margin-top: 0;
  margin-bottom: 15px;
}

.blog-meta {
  display: flex;
  gap: 20px;
  color: #666;
  font-size: 14px;
}

.blog-content {
  line-height: 1.8;
  color: #333;
}

.blog-content p {
  margin-bottom: 20px;
}

.blog-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.action-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #f5f5f5;
}

.like-btn {
  color: #f56c6c;
  border-color: #f56c6c;
}

.favorite-btn {
  color: #e6a23c;
  border-color: #e6a23c;
}
</style>
