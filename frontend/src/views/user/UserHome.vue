<template>
  <div class="user-home-container">
    <h2 class="page-title">推荐文章</h2>
    
    <div class="blog-list">
      <div v-for="(blog, index) in blogs" :key="index" class="blog-card">
        <h3 class="blog-title">{{ blog.title }}</h3>
        <p class="blog-excerpt">{{ blog.excerpt }}</p>
        <div class="blog-meta">
          <span class="blog-author">{{ blog.author }}</span>
          <span class="blog-date">{{ blog.date }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 模拟博客数据
const blogs = ref([
  {
    id: 1,
    title: '这是第一篇博客文章',
    excerpt: '这是文章的摘要内容，点击可以查看完整博客...',
    author: '张三',
    date: '2023-11-01'
  },
  {
    id: 2,
    title: '如何提高编程效率',
    excerpt: '通过这些实用技巧，你可以显著提高编码速度和质量...',
    author: '李四',
    date: '2023-10-28'
  },
  {
    id: 3,
    title: '人工智能的未来发展',
    excerpt: '随着技术的不断进步，人工智能将在未来扮演越来越重要的角色...',
    author: '王五',
    date: '2023-10-25'
  }
])
</script>

<style scoped>
.user-home-container {
  max-width: 1000px;
  margin: 0 auto;
}

.page-title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
}

.blog-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.blog-card {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.blog-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.blog-title {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
  color: #333;
}

.blog-excerpt {
  margin-bottom: 15px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.blog-meta {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 13px;
}
</style>
