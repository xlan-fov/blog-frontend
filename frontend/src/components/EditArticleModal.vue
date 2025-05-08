<template>
  <!-- 编辑按钮，点击时将open设为true，显示模态框 -->
  <button @click="open = true">编辑</button>
  
  <!-- 模态框，v-if控制显示/隐藏 -->
  <div v-if="open" class="modal">
    <h3>编辑文章</h3>
    
    <!-- 标题输入框 -->
    <input v-model="form.title" placeholder="标题" />
    
    <!-- 内容文本框 -->
    <textarea v-model="form.content" rows="6" placeholder="内容"></textarea>
    
    <!-- 按钮组 -->
    <div class="modal-actions">
      <button @click="save">保存</button>
      <button @click="open = false">取消</button>
    </div>
  </div>
</template>
<script setup>
// 导入Vue响应式API
import { ref, watch } from 'vue'
// 导入文章API
import { updateArticle } from '@/api/article'

// 定义组件属性
const props = defineProps({ article: Object })

// 定义组件事件
const emit = defineEmits(['saved'])

// 模态框是否打开的标志
const open = ref(false)

// 表单数据对象
const form = ref({ title: '', content: '' })

// 监听article属性变化，同步到表单数据
watch(() => props.article, v => {
  form.value.title   = v.title
  form.value.content = v.content
})

// 保存文章的函数
async function save() {
  // 调用API更新文章
  await updateArticle(props.article.id, form.value)
  // 触发saved事件通知父组件
  emit('saved')
  // 关闭模态框
  open.value = false
}
</script>