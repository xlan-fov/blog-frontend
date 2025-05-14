<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人空间</h2>
          <el-button type="primary" @click="handleEdit">编辑资料</el-button>
        </div>
      </template>

      <div class="profile-content">
        <div class="avatar-section">
          <el-avatar :size="100" :src="user.avatar" />
          <h3>{{ user.username }}</h3>
        </div>

        <div class="info-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDate(user.registerTime) }}</el-descriptions-item>
            <el-descriptions-item label="个人简介">{{ user.bio }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-card>

    <!-- 编辑资料对话框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑资料"
      width="500px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef">
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
            <div v-else class="avatar-uploader-icon">
              <!-- 使用 + 符号替代图标 -->
              <span style="font-size: 28px;">+</span>
            </div>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="个人简介" prop="bio">
          <el-input
            v-model="editForm.bio"
            type="textarea"
            :rows="4"
            placeholder="请输入个人简介"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveProfile">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 导入Vue的响应式API和生命周期钩子
import { ref, reactive, onMounted } from 'vue'
// 导入Element Plus消息组件
import { ElMessage } from 'element-plus'
// 导入用户相关API函数
import { getProfile, updateProfile } from '@/api/user'

// 创建响应式的用户数据对象
const user = ref({
  username: '',
  avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
  bio: '',
  registerTime: ''
})

// 对话框显示状态
const dialogVisible = ref(false)
// 表单引用
const editFormRef = ref(null)

// 编辑表单数据
const editForm = reactive({
  avatar: '',
  bio: ''
})

// 表单校验规则
const rules = {
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

// 格式化日期函数
const formatDate = (date) => {
  if (!date) return '未知'
  return new Date(date).toLocaleString()
}

// 打开编辑对话框
const handleEdit = () => {
  editForm.avatar = user.value.avatar
  editForm.bio = user.value.bio
  dialogVisible.value = true
}

// 上传头像前的处理函数
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return false
  }

  // 模拟上传成功
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    editForm.avatar = reader.result
  }
  return false // 阻止默认上传行为
}

// 保存个人资料
async function saveProfile() {
  try {
    // 调用API更新用户资料
    await updateProfile({ 
      bio: editForm.bio,
      avatar: editForm.avatar 
    })
    
    // 更新本地用户数据
    user.value.bio = editForm.bio
    user.value.avatar = editForm.avatar
    
    // 关闭对话框并显示成功消息
    dialogVisible.value = false
    ElMessage.success('资料更新成功')
  } catch (error) {
    ElMessage.error('更新失败：' + (error.message || '未知错误'))
  }
}

// 组件挂载时获取用户资料
onMounted(async () => {
  try {
    // 调用API获取用户资料
    const res = await getProfile()
    // 更新user的值
    user.value = res.data || {
      username: '测试用户',
      avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      bio: '这是一个测试用户简介',
      registerTime: new Date().toISOString()
    }
  } catch (error) {
    console.error('获取用户资料失败:', error)
    ElMessage.error('获取用户资料失败')
  }
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-content {
  display: flex;
  gap: 40px;
  padding: 20px 0;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.info-section {
  flex: 1;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
}

.avatar-uploader .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.avatar-uploader .avatar-uploader-icon:hover {
  border-color: #409eff;
}

@media screen and (max-width: 768px) {
  .profile-container {
    padding: 10px;
  }

  .card-header {
    flex-direction: column;
    gap: 10px;
    text-align: center;
  }

  .profile-content {
    flex-direction: column;
    gap: 20px;
  }

  .avatar-section {
    margin-bottom: 20px;
  }

  .el-descriptions {
    width: 100%;
  }

  .el-button {
    width: 100%;
    margin-bottom: 10px;
  }
}
</style>
