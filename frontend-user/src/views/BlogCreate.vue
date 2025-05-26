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
        <el-button type="info" @click="handleSaveDraft">保存草稿</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useBlogStore } from '@/stores/blog'
import { useUserStore } from '@/stores/user'
import E from 'wangeditor'

const router = useRouter()
const blogStore = useBlogStore()
const userStore = useUserStore()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

onMounted(() => {
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

  // 配置编辑器事件
  editor.config.onchange = (html) => {
    form.content = html
  }

  // 创建编辑器
  editor.create()
})

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
  }
})

// 保存草稿
const handleSaveDraft = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 检查用户是否已登录
  if (!userStore.userInfo || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await blogStore.createBlog({
      title: form.title,
      content: form.content,
      status: 'draft'
    })
    ElMessage.success('草稿保存成功')
    router.push('/dashboard/blog')
  } catch (error) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  }
}

// 发布博客
const handlePublish = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 检查用户是否已登录
  if (!userStore.userInfo || !userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    // 使用ElMessageBox.alert而不是confirm，因为alert在某些情况下层级显示更可靠
    await ElMessageBox.alert('确定要发布这篇博客吗？', '确认发布', {
      confirmButtonText: '确定发布',
      showCancelButton: true,
      cancelButtonText: '取消',
      type: 'info',
      zIndex: 10001,
      closeOnClickModal: false,
      appendToBody: true,
      modalAppendToBody: true
    })

    console.log('准备发布博客:', {
      title: form.title,
      content: form.content
    })
    
    // 直接调用createBlog方法，而不是publishBlog
    await blogStore.createBlog({
      title: form.title.trim(),
      content: form.content,
      status: 'published' // 直接设置状态为published
    })
    
    ElMessage.success('发布成功')
    router.push('/dashboard/blog')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error('发布失败: ' + (error.message || '未知错误'))
    }
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

/* 确保wangeditor的弹窗显示在最上层 */
:deep(.w-e-menu-panel) {
  z-index: 10000 !important;
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

<!-- 添加全局样式，确保优先级最高 -->
<style>
/* 确保MessageBox显示在最上层 */
body .el-message-box__wrapper {
  z-index: 10000 !important;
  position: fixed !important;
}

body .el-overlay {
  z-index: 9999 !important;
  position: fixed !important;
}

body .v-modal {
  z-index: 9998 !important;
  position: fixed !important;
}

/* 禁用可能干扰弹窗的其他元素 */
body.el-popup-parent--hidden .blog-create {
  z-index: auto !important;
}

/* 确保wangeditor不会覆盖弹窗 */
.w-e-toolbar, .w-e-text-container, .w-e-menu-panel {
  z-index: 100 !important;
}

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
</style>