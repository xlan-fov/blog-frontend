<template>
  <div class="blog-create">
    <el-form :model="form" label-width="80px" class="blog-form">
      <el-form-item label="标题">
        <el-input v-model="form.title" placeholder="请输入博客标题" />
      </el-form-item>
      <el-form-item label="内容" class="editor-wrapper">
        <div id="editor"></div>
      </el-form-item>
      <div class="button-wrapper">
      <el-form-item>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useBlogStore } from '@/stores/blog'
import E from 'wangeditor'

const router = useRouter()
const blogStore = useBlogStore()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

onMounted(() => {
  editor = new E('#editor')
  editor.config.height = 350
  editor.config.placeholder = '请输入博客内容'
  editor.config.onchange = (html) => {
    form.content = html
  }
  editor.create()
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    return
  }

  try {
    await blogStore.createBlog({
      content: form.content
    })
    ElMessage.success('创建成功')
    router.push('/dashboard/blog')
  } catch (error) {
    ElMessage.error('创建失败: ' + (error.message || '未知错误'))
  }
}

const handleCancel = () => {
  router.push('/dashboard/blog')
}
</script>

<style scoped>
.blog-create {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: 100vh;
}

.blog-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.editor-wrapper {
  flex: 1;
  min-height: 0;
  margin-bottom: 80px; /* 为按钮预留空间 */
}

.button-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 15px 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

:deep(.w-e-text-container) {
  height: 350px !important;
}

:deep(.w-e-toolbar) {
  position: sticky;
  top: 0;
  z-index: 1;
  background-color: #fff;
}

/* 2K分辨率 */
@media screen and (min-width: 1920px) {
  .blog-create {
    max-width: 1800px;
    margin: 0 auto;
  }

  :deep(.w-e-text-container) {
    height: 450px !important;
  }
}

/* 1080P分辨率 */
@media screen and (max-width: 1919px) {
  .blog-create {
    max-width: 1200px;
    margin: 0 auto;
  }

  :deep(.w-e-text-container) {
    height: 350px !important;
  }
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .blog-create {
    padding: 10px;
  }

  .button-wrapper {
    padding: 10px;
  }

  :deep(.w-e-text-container) {
    height: 300px !important;
  }

  .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>