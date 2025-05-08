<template>
  <!-- 删除按钮，点击时将open设为true，显示确认对话框 -->
  <button @click="open = true">删除</button>
  
  <!-- 确认对话框，v-if控制显示/隐藏 -->
  <div v-if="open" class="modal">
    <p>确定要删除这篇文章？</p>
    
    <!-- 按钮组 -->
    <div class="modal-actions">
      <button @click="confirmDelete">确定</button>
      <button @click="open = false">取消</button>
    </div>
  </div>
</template>
<script setup>
// 导入Vue响应式API
import { ref } from 'vue'
// 导入文章API
import { deleteArticle } from '@/api/article'

// 定义组件属性
const props = defineProps({ id: Number })

// 定义组件事件
const emit = defineEmits(['deleted'])

// 对话框是否打开的标志
const open = ref(false)

// 确认删除的函数
async function confirmDelete() {
  // 调用API删除文章
  await deleteArticle(props.id)
  // 触发deleted事件通知父组件
  emit('deleted')
  // 关闭对话框
  open.value = false
}
</script>