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
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
          <el-button type="success" @click="handlePublish" :loading="publishing">发布</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </div>
    </el-form>

    <!-- 自定义确认对话框 -->
    <div v-if="showConfirm" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.8); z-index: 999999; display: flex; align-items: center; justify-content: center;">
      <div style="background: white; padding: 30px; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); min-width: 300px; text-align: center;">
        <h3 style="margin: 0 0 20px 0; color: #333;">确认发布</h3>
        <p style="margin: 0 0 30px 0; color: #666;">确定要发布这篇博客吗？</p>
        <div style="display: flex; gap: 10px; justify-content: center;">
          <button @click="cancelPublish" style="padding: 8px 16px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer;">取消</button>
          <button @click="confirmPublish" :disabled="publishing" style="padding: 8px 16px; border: none; background: #409EFF; color: white; border-radius: 4px; cursor: pointer;">
            {{ publishing ? '发布中...' : '确定发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import blogApi from '@/api/blog'
import adminApi from '@/api/admin'
import E from 'wangeditor'

const router = useRouter()
const route = useRoute()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

// 添加状态变量
const saving = ref(false)
const publishing = ref(false)
const showConfirm = ref(false)

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

  saving.value = true
  try {
    console.log('准备保存博客，ID:', route.params.id, '数据:', form)
    
    const res = await blogApi.updateBlog(route.params.id, {
      title: form.title.trim(),
      content: form.content,
      status: 'draft'
    })
    
    console.log('保存响应:', res)
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      goBack()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    if (error.response && error.response.status === 403) {
      ElMessage.error('无权限编辑此博客')
    } else {
      ElMessage.error('保存失败: ' + (error.message || '未知错误'))
    }
  } finally {
    saving.value = false
  }
}

const handlePublish = () => {
  console.log('发布按钮被点击')
  
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  // 显示自定义确认框
  showConfirm.value = true
}

const cancelPublish = () => {
  console.log('取消发布')
  showConfirm.value = false
}

const confirmPublish = async () => {
  try {
    publishing.value = true
    console.log('用户确认发布，开始发布博客，ID:', route.params.id)
    
    // 先保存博客内容
    const saveRes = await blogApi.updateBlog(route.params.id, {
      title: form.title.trim(),
      content: form.content,
      status: 'draft'
    })
    
    if (saveRes.code !== 200) {
      ElMessage.error(saveRes.message || '保存博客内容失败')
      return
    }
    
    // 使用管理员专用接口发布博客
    const publishRes = await adminApi.publishArticle(route.params.id)
    
    if (publishRes.code === 200) {
      showConfirm.value = false
      ElMessage.success('博客发布成功')
      setTimeout(() => {
        goBack()
      }, 1000)
    } else {
      ElMessage.error(publishRes.message || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(`发布失败: ${error.message || '未知API错误'}`)
  } finally {
    publishing.value = false
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
