<template>
  <div class="blog-edit">
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
          <el-button type="info" @click="handleExportPDF" :loading="exporting" :disabled="!route.params.id">导出PDF</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
          <el-button type="success" @click="handlePublish" :loading="publishing">发布</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </div>
    </el-form>

    <!-- 自定义确认对话框 -->
    <div v-if="showConfirm" class="confirm-dialog">
      <div class="confirm-content">
        <h3>确认发布</h3>
        <p>确定要发布这篇博客吗？</p>
        <div class="confirm-buttons">
          <el-button @click="cancelPublish">取消</el-button>
          <el-button type="primary" @click="confirmPublish" :loading="publishing">
            {{ publishing ? '发布中...' : '确定发布' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useBlogStore } from '@/stores/blog'
import { useUserStore } from '@/stores/user'
import E from 'wangeditor'

const router = useRouter()
const route = useRoute()
const blogStore = useBlogStore()
const userStore = useUserStore()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

// 状态变量
const saving = ref(false)
const publishing = ref(false)
const showConfirm = ref(false)
const exporting = ref(false) // 新增：PDF导出状态

// 监听内容变化，触发自动保存
watch(() => form.content, (newContent) => {
  if (newContent) {
    blogStore.startAutoSave({
      id: route.params.id,
      title: form.title,
      content: newContent
    })
  }
})

onMounted(async () => {
  // 初始化编辑器
  editor = new E('#editor')
  
  // 基础配置
  editor.config.height = 350
  editor.config.placeholder = '请输入博客内容'
  editor.config.zIndex = 100
  
  // 配置菜单栏
  editor.config.menus = [
    'head',
    'bold',
    'fontSize',
    'fontName',
    'italic',
    'underline',
    'strikeThrough',
    'foreColor',
    'backColor',
    'link',
    'list',
    'justify',
    'quote',
    'emoticon',
    'image',
    'table',
    'code',
    'undo',
    'redo'
  ]

  // 配置编辑器样式
  editor.config.styleWithCSS = true  // 使用 style 标签
  editor.config.pasteFilterStyle = false  // 不过滤粘贴内容的样式
  editor.config.pasteIgnoreImg = false  // 允许粘贴图片
  editor.config.pasteTextHandle = function(content) {
    return content
  }

  // 配置图片上传
  editor.config.uploadImgServer = '/api/common/upload'
  editor.config.uploadFileName = 'file'
  editor.config.uploadImgMaxSize = 5 * 1024 * 1024
  editor.config.uploadImgAccept = ['jpg', 'jpeg', 'png', 'gif', 'webp']
  editor.config.uploadImgHeaders = {
    Authorization: localStorage.getItem('token') || ''
  }
  
  // 配置图片上传钩子
  editor.config.uploadImgHooks = {
    before: function(xhr) {
      console.log('准备上传图片')
    },
    customInsert: function(insertImgFn, result) {
      console.log('图片上传结果:', result)
      if (result.code === 200 && result.data && result.data.url) {
        insertImgFn(result.data.url)
      } else {
        ElMessage.error('图片上传失败：' + (result.msg || '未知错误'))
      }
    },
    fail: function(xhr, editor, resData) {
      console.error('图片上传失败:', resData)
      ElMessage.error('图片上传失败')
    },
    error: function(xhr, editor, resData) {
      console.error('图片上传错误:', resData)
      ElMessage.error('图片上传错误')
    },
    timeout: function(xhr, editor, resData) {
      console.error('图片上传超时:', resData)
      ElMessage.error('图片上传超时')
    }
  }

  // 禁用网络图片功能
  editor.config.showLinkImg = false
  editor.config.showLinkImgAlt = false
  editor.config.showLinkImgHref = false
  editor.config.showLinkImgSize = false
  editor.config.showLinkImgTitle = false
  editor.config.showLinkImgType = false
  editor.config.showLinkImgUrl = false
  editor.config.showLinkImgWidth = false
  editor.config.showLinkImgHeight = false
  editor.config.showLinkImgBorder = false
  editor.config.showLinkImgMargin = false
  editor.config.showLinkImgPadding = false
  editor.config.showLinkImgFloat = false
  editor.config.showLinkImgAlign = false
  editor.config.showLinkImgAlt = false
  editor.config.showLinkImgHref = false
  editor.config.showLinkImgSize = false
  editor.config.showLinkImgTitle = false
  editor.config.showLinkImgType = false
  editor.config.showLinkImgUrl = false
  editor.config.showLinkImgWidth = false
  editor.config.showLinkImgHeight = false
  editor.config.showLinkImgBorder = false
  editor.config.showLinkImgMargin = false
  editor.config.showLinkImgPadding = false
  editor.config.showLinkImgFloat = false
  editor.config.showLinkImgAlign = false

  // 配置编辑器事件
  editor.config.onchange = (html) => {
    form.content = html
  }

  // 创建编辑器
  editor.create()

  // 如果是编辑模式，加载博客数据
  const blogId = route.params.id
  if (blogId) {
    try {
      const blog = await blogStore.getBlogById(blogId)
      if (blog) {
        form.title = blog.title
        form.content = blog.content
        editor.txt.html(blog.content)
      } else {
        ElMessage.error('博客不存在')
        goBack()
      }
    } catch (error) {
      console.error('获取博客失败:', error)
      ElMessage.error('获取博客失败')
      goBack()
    }
  }
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
  blogStore.stopAutoSave()
})

const goBack = () => {
  router.push('/dashboard/blog')
}

const validateForm = () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return false
  }

  if (!userStore.userInfo || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return false
  }

  return true
}

const handleSaveDraft = async () => {
  if (!validateForm()) return

  saving.value = true
  try {
    await blogStore.saveDraft({
      id: route.params.id,
      title: form.title.trim(),
      content: form.content,
      status: 'draft'
    })
    ElMessage.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const handleSave = async () => {
  if (!validateForm()) return

  saving.value = true
  try {
    await blogStore.updateBlog(route.params.id, {
      title: form.title.trim(),
      content: form.content,
      status: 'draft'
    })
    ElMessage.success('保存成功')
    goBack()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

const handlePublish = () => {
  if (!validateForm()) return
  showConfirm.value = true
}

const cancelPublish = () => {
  showConfirm.value = false
}

const confirmPublish = async () => {
  try {
    publishing.value = true
    
    const blogId = route.params.id
    await blogStore.publishBlog({
      id: blogId,
      title: form.title.trim(),
      content: form.content
    })
    
    showConfirm.value = false
    ElMessage.success('博客发布成功')
    setTimeout(() => {
      goBack()
    }, 1000)
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(`发布失败: ${error.message || '未知错误'}`)
  } finally {
    publishing.value = false
  }
}

// 新增：导出PDF功能
const handleExportPDF = async () => {
  // 检查是否有有效的博客ID
  const blogId = route.params.id
  if (!blogId) {
    ElMessage.warning('请先保存博客后再导出PDF')
    return
  }

  exporting.value = true
  try {
    // 使用fetch API直接调用后端接口以处理二进制文件流
    const response = await fetch(`/api/export/${blogId}`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (!response.ok) {
      throw new Error(`导出失败: ${response.statusText}`)
    }
    
    // 获取文件流
    const blob = await response.blob()
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.style.display = 'none'
    a.href = url
    
    // 从响应头获取文件名，如果没有则使用博客标题
    const contentDisposition = response.headers.get('Content-Disposition')
    let filename = `${form.title || 'blog'}.pdf`
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, '')
      }
    }
    
    a.download = decodeURIComponent(filename)
    document.body.appendChild(a)
    a.click()
    
    // 清理
    window.URL.revokeObjectURL(url)
    document.body.removeChild(a)
    
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage.error(`导出PDF失败: ${error.message || '未知错误'}`)
  } finally {
    exporting.value = false
  }
}
</script>

<style scoped>
.blog-edit {
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

.confirm-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  z-index: 999999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.confirm-content {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  min-width: 300px;
  text-align: center;
}

.confirm-content h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.confirm-content p {
  margin: 0 0 30px 0;
  color: #666;
}

.confirm-buttons {
  display: flex;
  gap: 10px;
  justify-content: center;
}

/* 确保wangeditor的弹窗显示在最上层 */
:deep(.w-e-menu-panel) {
  z-index: 10000 !important;
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

/* 编辑器内容样式 */
:deep(.w-e-text) {
  padding: 10px;
}

:deep(.w-e-text p) {
  margin: 1em 0;
}

:deep(.w-e-text strong),
:deep(.w-e-text b) {
  font-weight: bold !important;
}

:deep(.w-e-text em),
:deep(.w-e-text i) {
  font-style: italic !important;
}

:deep(.w-e-text u) {
  text-decoration: underline !important;
}

:deep(.w-e-text s) {
  text-decoration: line-through !important;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .blog-edit {
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

<!-- 添加全局样式 -->
<style>
/* 确保wangeditor的弹窗显示在最上层 */
.w-e-menu-panel {
  z-index: 10000 !important;
}

.w-e-text-container {
  z-index: 1 !important;
}

.w-e-toolbar {
  z-index: 2 !important;
}

/* 编辑器内容样式 */
.w-e-text {
  padding: 10px;
}

.w-e-text p {
  margin: 1em 0;
}

.w-e-text strong,
.w-e-text b {
  font-weight: bold !important;
}

.w-e-text em,
.w-e-text i {
  font-style: italic !important;
}

.w-e-text u {
  text-decoration: underline !important;
}

.w-e-text s {
  text-decoration: line-through !important;
}

/* 确保MessageBox显示在最上层 */
body .el-message-box__wrapper {
  z-index: 10001 !important;
  position: fixed !important;
}

body .el-overlay {
  z-index: 10000 !important;
  position: fixed !important;
}

body .v-modal {
  z-index: 9999 !important;
  position: fixed !important;
}
</style>