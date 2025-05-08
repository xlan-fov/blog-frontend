<template>
  <div>
    <!-- 文章标题输入框 -->
    <input v-model="meta.title" placeholder="标题" />
    
    <!-- 分类选择组件 -->
    <CategorySelect v-model="meta.category" />
    
    <!-- 标签选择组件 -->
    <TagSelector v-model="meta.tags" />
    
    <!-- 富文本编辑器组件 -->
    <RichTextEditor v-model="content" />
    
    <!-- 发布按钮 -->
    <button @click="publish">发布</button>
  </div>
</template>
<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, reactive, onBeforeUnmount } from 'vue'
// 导入文章相关API函数
import { fetchArticle, updateArticle, createArticle } from '@/api/article'
// 导入自定义组件
import CategorySelect from '@/components/CategorySelect.vue'
import TagSelector from '@/components/TagSelector.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'

// 创建响应式的文章元数据对象
const meta = reactive({ title:'', category:null, tags:[] })
// 创建响应式的文章内容
const content = ref('')
// 声明定时器变量，用于自动保存草稿
let draftTimer

// 设置自动保存草稿的定时器，每30秒执行一次
draftTimer = setInterval(() => {
  // ...调用 createArticle 或 updateArticle 保存草稿...
}, 30000)

// 组件卸载前清除定时器，防止内存泄漏
onBeforeUnmount(() => clearInterval(draftTimer))

// 发布文章的函数
async function publish() {
  // ...内容合规检测 + 发布接口...
}
</script>
