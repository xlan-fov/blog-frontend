<template>
  <div class="blog-editor">
    <div class="editor-header">
      <h2>{{ isNewBlog ? '新建博客' : '编辑博客' }}</h2>
      <div class="editor-actions">
        <button class="cancel-btn" @click="cancelEdit">取消</button>
        <button class="save-btn" @click="saveDraft">保存草稿</button>
        <button class="publish-btn" @click="publishBlog">发布</button>
      </div>
    </div>
    
    <div class="editor-body">
      <div class="form-item">
        <label>标题</label>
        <input v-model="blogData.title" placeholder="请输入博客标题" />
      </div>
      
      <div class="form-item">
        <label>内容</label>
        <textarea 
          v-model="blogData.content" 
          placeholder="请输入博客内容" 
          rows="15"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const blogId = route.params.id

// 是否为新建博客
const isNewBlog = computed(() => blogId === 'new')

// 博客数据
const blogData = ref({
  title: '',
  content: '',
  status: '未发布'
})

// 获取博客数据
onMounted(() => {
  if (!isNewBlog.value) {
    // 实际项目中应该调用API获取博客数据
    // 这里模拟获取数据
    blogData.value = {
      id: blogId,
      title: '博客标题',
      content: '博客内容示例',
      status: '未发布'
    }
  }
})

// 取消编辑
function cancelEdit() {
  if (confirm('确定要取消编辑吗？未保存的内容将会丢失')) {
    router.push({ name: 'UserContentManage' })
  }
}

// 保存草稿
function saveDraft() {
  if (!blogData.value.title || !blogData.value.content) {
    alert('标题和内容不能为空')
    return
  }
  
  // 实际项目中应该调用API保存草稿
  alert('保存成功')
  router.push({ name: 'UserContentManage' })
}

// 发布博客
function publishBlog() {
  if (!blogData.value.title || !blogData.value.content) {
    alert('标题和内容不能为空')
    return
  }
  
  // 实际项目中应该调用API发布博客
  blogData.value.status = '已发布'
  alert('发布成功')
  router.push({ name: 'UserContentManage' })
}
</script>

<style scoped>
.blog-editor {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.editor-header h2 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.editor-actions {
  display: flex;
  gap: 10px;
}

.cancel-btn {
  padding: 0 15px;
  height: 36px;
  background-color: #f5f5f5;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn {
  padding: 0 15px;
  height: 36px;
  background-color: #fff8e6;
  color: #d48806;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.publish-btn {
  padding: 0 15px;
  height: 36px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.form-item input {
  width: 100%;
  height: 40px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 16px;
  box-sizing: border-box;
}

.form-item textarea {
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 12px;
  font-size: 16px;
  box-sizing: border-box;
  resize: vertical;
  font-family: inherit;
}
</style>
