<template>
  <div class="admin-blog-edit">
    <div class="page-header">
      <h2>编辑博客</h2>
      <el-button @click="goBack">返回列表</el-button>
    </div>
    
    <el-form :model="form" label-width="80px" class="blog-form">
      <el-form-item label="标题" required>
        <el-input v-model="form.title" placeholder="请输入博客标题" />
      </el-form-item>
      <el-form-item label="内容" class="editor-wrapper" required>
        <div id="editor"></div>
      </el-form-item>
      <div class="button-wrapper">
        <el-form-item>
          <el-button type="primary" @click="handleSave">保存</el-button>
          <el-button type="success" @click="handlePublish">发布</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import blogApi from '@/api/blog'
import E from 'wangeditor'

const router = useRouter()
const route = useRoute()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

onMounted(async () => {
  editor = new E('#editor')
  editor.config.height = 350
  editor.config.placeholder = '请输入博客内容'
  editor.config.onchange = (html) => {
    form.content = html
  }
  editor.create()

  const blogId = route.params.id
  try {
    const res = await blogApi.getBlogById(blogId)
    if (res.code === 200 && res.data) {
      form.title = res.data.title
      form.content = res.data.content
      editor.txt.html(res.data.content)
    } else {
      ElMessage.error('博客不存在')
      goBack()
    }
  } catch (error) {
    console.error('获取博客失败:', error)
    ElMessage.error('获取博客失败')
    goBack()
  }
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})

const goBack = () => {
  router.push('/admin/blog-management')
}

const handleSave = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    const res = await blogApi.updateBlog(route.params.id, {
      title: form.title,
      content: form.content,
      status: 'draft'
    })
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      goBack()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  }
}

const handlePublish = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    await ElMessageBox.confirm('确定要发布这篇博客吗？', '确认发布', {
      confirmButtonText: '确定发布',
      cancelButtonText: '取消',
      type: 'info'
    })

    const res = await blogApi.updateBlog(route.params.id, {
      title: form.title,
      content: form.content,
      status: 'published'
    })
    
    if (res.code === 200) {
      ElMessage.success('发布成功')
      goBack()
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败: ' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
/* 与 AdminBlogCreate.vue 相同的样式 */
.admin-blog-edit {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e6e6e6;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.blog-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  max-width: 1200px;
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
  padding: 15px 20px;
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

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .admin-blog-edit {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
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
