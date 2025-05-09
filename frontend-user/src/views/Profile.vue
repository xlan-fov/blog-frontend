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
          <el-avatar :size="100" :src="userInfo.avatar" />
          <h3>{{ userInfo.username }}</h3>
        </div>

        <div class="info-section">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDate(userInfo.registerTime) }}</el-descriptions-item>
            <el-descriptions-item label="个人简介">{{ userInfo.bio }}</el-descriptions-item>
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
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
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
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const dialogVisible = ref(false)
const editFormRef = ref(null)

const userInfo = computed(() => userStore.userInfo)

const editForm = reactive({
  avatar: userInfo.value.avatar,
  bio: userInfo.value.bio
})

const rules = {
  bio: [
    { max: 200, message: '个人简介不能超过200个字符', trigger: 'blur' }
  ]
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString()
}

const handleEdit = () => {
  editForm.avatar = userInfo.value.avatar
  editForm.bio = userInfo.value.bio
  dialogVisible.value = true
}

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

  // TODO: 替换为axios请求
  // 任务：实现上传头像的axios接口请求
  // 模拟上传成功
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    editForm.avatar = reader.result
  }
  return false
}

const handleSave = () => {
  // TODO: 替换为axios请求
  // 任务：实现更新个人资料的axios接口请求
  userStore.updateProfile(editForm.avatar, editForm.bio)
  dialogVisible.value = false
  ElMessage.success('保存成功')
}
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