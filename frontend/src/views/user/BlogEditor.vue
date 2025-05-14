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
import E from 'wangeditor'

const router = useRouter()
const route = useRoute()

const form = reactive({
  title: '',
  content: ''
})

let editor = null

onMounted(() => {
  // 初始化编辑器
  editor = new E('#editor')
  editor.config.height = 350
  editor.config.placeholder = '请输入博客内容'
  editor.config.onchange = (html) => {
    form.content = html
  }
  editor.create()

  // 如果有ID参数，说明是编辑模式
  const articleId = route.params.id
  if (articleId) {
    // 获取文章数据并填充表单
    // 实际项目中应该从API获取数据
    setTimeout(() => {
      form.title = '文章标题示例'
      form.content = '<p>这是文章内容示例...</p>'
      editor.txt.html(form.content)
    }, 300)
  }
})

onBeforeUnmount(() => {
  // 销毁编辑器实例
  if (editor) {
    editor.destroy()
  }
})

// 表单提交
const handleSubmit = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning('请填写完整信息')
    return
  }

  try {
    // 实际项目中应该调用API保存文章
    const isEdit = !!route.params.id
    
    if (isEdit) {
      // 更新文章
      ElMessage.success('更新成功')
    } else {
      // 创建新文章
      ElMessage.success('创建成功')
    }
    
    // 返回到内容管理页面
    router.push('/user/content')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 取消按钮
const handleCancel = () => {
  router.push('/user/content')
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

/* 响应式布局调整 */
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
