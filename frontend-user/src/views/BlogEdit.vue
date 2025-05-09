<template>
  <div class="blog-edit">
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useBlogStore } from '@/stores/blog'
import E from 'wangeditor'

const router = useRouter()
const route = useRoute()
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

  const blogId = route.params.id
  const blog = blogStore.blogs.find(blog => blog.id === blogId)
  if (blog) {
    form.title = blog.title
    form.content = blog.content
    editor.txt.html(blog.content)
  } else {
    ElMessage.error('博客不存在')
    router.push('/blog')
  }
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})

const handleSubmit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    // TODO: 替换为axios请求
    // 任务：实现更新博客的axios接口请求
    await blogStore.updateBlog(route.params.id, {
      title: form.title,
      content: form.content
    })
    ElMessage.success('更新成功')
    router.push('/blog')
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleCancel = () => {
  router.push('/blog')
}
</script>

<style scoped>
.blog-edit {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: calc(100vh - 100px);
  margin: 20px;
}

.blog-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.blog-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
}

.blog-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.blog-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #409EFF inset;
}

.editor-wrapper {
  flex: 1;
  min-height: 0;
  margin-bottom: 80px;
}

.button-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 16px 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.button-wrapper .el-button {
  min-width: 100px;
}

:deep(.w-e-text-container) {
  height: 350px !important;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

:deep(.w-e-toolbar) {
  position: sticky;
  top: 0;
  z-index: 1;
  background-color: #fff;
  border-radius: 4px 4px 0 0;
  border: 1px solid #dcdfe6;
  border-bottom: none;
}

:deep(.w-e-bar) {
  border-bottom: 1px solid #dcdfe6;
}

:deep(.w-e-bar-item) {
  border-radius: 4px;
}

:deep(.w-e-bar-item:hover) {
  background-color: #f5f7fa;
}

/* 2K分辨率 */
@media screen and (min-width: 1920px) {
  .blog-edit {
    max-width: 1800px;
    margin: 20px auto;
  }

  :deep(.w-e-text-container) {
    height: 450px !important;
  }
}

/* 1080P分辨率 */
@media screen and (max-width: 1919px) {
  .blog-edit {
    max-width: 1200px;
    margin: 20px auto;
  }

  :deep(.w-e-text-container) {
    height: 350px !important;
  }
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .blog-edit {
    padding: 10px;
    margin: 10px;
  }

  .button-wrapper {
    padding: 10px;
    flex-direction: column;
  }

  .button-wrapper .el-button {
    width: 100%;
  }

  :deep(.w-e-text-container) {
    height: 300px !important;
  }
}
</style> 