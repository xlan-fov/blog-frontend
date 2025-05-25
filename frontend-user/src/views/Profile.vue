<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="userStore.userInfo.avatar" />
              <div class="avatar-upload">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  :http-request="uploadAvatar"
                >
                  <el-icon class="upload-icon"><Camera /></el-icon>
                </el-upload>
              </div>
            </div>
            <h2 class="username">{{ userStore.userInfo.username }}</h2>
            <div class="role-tag">
              <el-tag type="primary">普通用户</el-tag>
            </div>
          </div>
          
          <div class="profile-info">
            <div class="info-item">
              <div class="label">注册时间</div>
              <div class="value">{{ formatDate(userStore.userInfo.registerTime) }}</div>
            </div>
            <div class="info-item">
              <div class="label">最后登录</div>
              <div class="value">{{ formatDate(userStore.userInfo.lastLoginTime) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>个人资料</span>
              <el-button type="primary" @click="isEditing = !isEditing">
                {{ isEditing ? '取消' : '编辑' }}
              </el-button>
            </div>
          </template>
          
          <el-form 
            :model="profileForm" 
            label-width="100px"
            :disabled="!isEditing"
          >
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" :disabled="!isEditing" />
            </el-form-item>
            
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" disabled />
            </el-form-item>
            
            <el-form-item label="个人简介">
              <el-input 
                v-model="profileForm.bio" 
                type="textarea" 
                :rows="4"
                :disabled="!isEditing"
              />
            </el-form-item>
            
            <el-form-item v-if="isEditing">
              <el-button type="primary" @click="saveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card class="security-card">
          <template #header>
            <div class="card-header">
              <span>安全设置</span>
            </div>
          </template>
          
          <div class="security-items">
            <div class="security-item">
              <div class="security-item-info">
                <div class="title">修改密码</div>
                <div class="desc">定期修改密码可以保障账号安全</div>
              </div>
              <el-button @click="showChangePassword = true">修改</el-button>
            </div>
            
            <div class="security-item">
              <div class="security-item-info">
                <div class="title">绑定手机</div>
                <div class="desc">已绑定: {{ maskPhone(profileForm.phone) }}</div>
              </div>
              <el-button @click="showChangePhone = true">修改</el-button>
            </div>
            
            <div class="security-item">
              <div class="security-item-info">
                <div class="title">登录日志</div>
                <div class="desc">查看账号近期的登录记录</div>
              </div>
              <el-button @click="handleShowLoginLogs">查看</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePassword"
      title="修改密码"
      width="500px"
    >
      <el-form :model="passwordForm" label-width="100px" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showChangePassword = false">取消</el-button>
          <el-button type="primary" @click="changePassword" :loading="submitting">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改手机对话框 -->
    <el-dialog
      v-model="showChangePhone"
      title="修改手机号"
      width="500px"
    >
      <el-form :model="phoneForm" label-width="100px" :rules="phoneRules" ref="phoneFormRef">
        <el-form-item label="原手机号">
          <div>{{ maskPhone(userStore.userInfo.phone) }}</div>
        </el-form-item>
        
        <el-form-item label="新手机号" prop="newPhone">
          <el-input v-model="phoneForm.newPhone" />
        </el-form-item>
        
        <el-form-item label="验证码" prop="code">
          <div class="code-input">
            <el-input v-model="phoneForm.code" />
            <el-button type="primary" @click="sendCode" :disabled="countdown > 0">
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showChangePhone = false">取消</el-button>
          <el-button type="primary" @click="changePhone" :loading="submitting">
            确认修改
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 登录日志对话框 -->
    <el-dialog
      v-model="showLoginLogs"
      title="登录日志"
      width="700px"
    >
      <el-table :data="loginLogs" border style="width: 100%">
        <el-table-column prop="time" label="登录时间" width="180" />
        <el-table-column prop="ip" label="IP地址" width="150" />
        <el-table-column prop="location" label="地理位置" width="150" />
        <el-table-column prop="device" label="设备信息" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">
              {{ scope.row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { 
  getProfile, 
  updateProfile, 
  uploadAvatar as uploadAvatarApi, 
  changePassword as changePasswordApi, 
  changePhone as changePhoneApi,
  sendPhoneCode,
  getLoginLogs
} from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const isEditing = ref(false)
const submitting = ref(false)
const countdown = ref(0)

// 个人资料表单
const profileForm = reactive({
  username: userStore.userInfo.username || '用户',
  phone: userStore.userInfo.phone || '',
  bio: userStore.userInfo.bio || '这是我的个人简介'
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码校验规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 手机表单
const phoneForm = reactive({
  newPhone: '',
  code: ''
})

// 手机校验规则
const phoneRules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

// 对话框控制
const showChangePassword = ref(false)
const showChangePhone = ref(false)
const showLoginLogs = ref(false)
const passwordFormRef = ref(null)
const phoneFormRef = ref(null)

// 登录日志
const loginLogs = ref([])

// 获取登录日志
const fetchLoginLogs = async () => {
  try {
    const res = await getLoginLogs()
    if (res.code === 200 && res.data) {
      loginLogs.value = res.data.map(log => ({
        time: new Date(log.loginTime).toLocaleString(),
        ip: log.ip,
        location: log.location || '-',
        device: log.device || '-',
        status: log.status || 'success'
      }))
    } else {
      ElMessage.error(res.message || '获取登录日志失败')
    }
  } catch (error) {
    console.error('获取登录日志失败:', error)
    ElMessage.error('获取登录日志失败')
  }
}

// 显示登录日志对话框
const handleShowLoginLogs = async () => {
  showLoginLogs.value = true
  await fetchLoginLogs()
}

// 保存个人资料
const saveProfile = async () => {
  submitting.value = true
  try {
    const res = await updateProfile({
      id: userStore.userInfo.id,
      username: profileForm.username,
      description: profileForm.bio
    })
    
    if (res.code === 200) {
      // 重新获取用户信息
      const profileRes = await getProfile()
      if (profileRes.code === 200 && profileRes.data) {
        userStore.updateUserInfo({
          id: profileRes.data.id,
          username: profileRes.data.username,
          avatar: profileRes.data.avatar,
          bio: profileRes.data.description,
          phone: profileRes.data.phone,
          registerTime: profileRes.data.registerTime,
          lastLoginTime: profileRes.data.lastLoginTime
        })
      }
      isEditing.value = false
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('保存个人资料失败:', error)
    ElMessage.error('保存失败')
  } finally {
    submitting.value = false
  }
}

// 修改密码
const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await changePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      if (res.code === 200) {
        showChangePassword.value = false
        ElMessage.success('密码修改成功')
        // 重置表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
      } else {
        ElMessage.error(res.message || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败:', error)
      ElMessage.error('密码修改失败')
    } finally {
      submitting.value = false
    }
  })
}

// 发送验证码
const sendCode = async () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning('请输入新手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    const res = await sendPhoneCode(phoneForm.newPhone)
    if (res.code === 200) {
      countdown.value = 60
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
      ElMessage.success('验证码已发送')
    } else {
      ElMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    console.error('发送验证码失败:', error)
    ElMessage.error('验证码发送失败')
  }
}

// 修改手机号
const changePhone = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await changePhoneApi({
        newPhone: phoneForm.newPhone,
        code: phoneForm.code
      })
      
      if (res.code === 200) {
        // 重新获取用户信息
        const profileRes = await getProfile()
        if (profileRes.code === 200 && profileRes.data) {
          userStore.updateUserInfo({
            id: profileRes.data.id,
            username: profileRes.data.username,
            avatar: profileRes.data.avatar,
            bio: profileRes.data.description,
            phone: profileRes.data.phone,
            registerTime: profileRes.data.registerTime,
            lastLoginTime: profileRes.data.lastLoginTime
          })
        }
        showChangePhone.value = false
        ElMessage.success('手机号修改成功')
        // 重置表单
        phoneForm.newPhone = ''
        phoneForm.code = ''
      } else {
        ElMessage.error(res.message || '手机号修改失败')
      }
    } catch (error) {
      console.error('修改手机号失败:', error)
      ElMessage.error('手机号修改失败')
    } finally {
      submitting.value = false
    }
  })
}

// 上传头像前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('上传头像图片只能是图片格式!')
  }
  
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  
  return isImage && isLt2M
}

// 上传头像
const uploadAvatar = async (options) => {
  const file = options.file
  
  // 创建 FormData 对象
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const res = await uploadAvatarApi(formData)
    if (res.code === 200) {
      // 重新获取用户信息
      const profileRes = await getProfile()
      if (profileRes.code === 200 && profileRes.data) {
        userStore.updateUserInfo({
          id: profileRes.data.id,
          username: profileRes.data.username,
          avatar: profileRes.data.avatar,
          bio: profileRes.data.description,
          phone: profileRes.data.phone,
          registerTime: profileRes.data.registerTime,
          lastLoginTime: profileRes.data.lastLoginTime
        })
      }
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('上传头像失败:', error)
    ElMessage.error('头像上传失败')
  }
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  // 确保时间戳是数字
  const time = Number(timestamp)
  if (isNaN(time)) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 初始化
onMounted(async () => {
  try {
    console.log('开始获取用户信息...')
    const profileRes = await getProfile()
    console.log('获取到的用户信息:', profileRes)
    if (profileRes.code === 200 && profileRes.data) {
      // 更新 store 中的用户信息
      userStore.updateUserInfo({
        id: profileRes.data.id,
        username: profileRes.data.username,
        avatar: profileRes.data.avatar,
        bio: profileRes.data.description,
        phone: profileRes.data.phone,
        registerTime: profileRes.data.registerTime,
        lastLoginTime: profileRes.data.lastLoginTime
      })
      
      // 更新表单数据
      profileForm.username = profileRes.data.username
      profileForm.phone = profileRes.data.phone || ''
      profileForm.bio = profileRes.data.description || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
})
</script>

<style scoped>
.profile-container {
  padding: 16px;
}

.profile-card, .info-card, .security-card {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 16px;
}

.avatar-upload {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 32px;
  height: 32px;
  background-color: #ffffff;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-upload:hover {
  transform: scale(1.1);
}

.upload-icon {
  font-size: 16px;
  color: #409EFF;
}

.username {
  font-size: 20px;
  font-weight: bold;
  margin: 10px 0;
}

.role-tag {
  margin-bottom: 16px;
}

.profile-info {
  width: 100%;
  border-top: 1px solid #EBEEF5;
  padding-top: 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.security-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.security-item-info .title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.security-item-info .desc {
  font-size: 12px;
  color: #909399;
}

.code-input {
  display: flex;
  gap: 10px;
}

.code-input .el-input {
  flex: 1;
}

@media screen and (max-width: 992px) {
  .el-col {
    width: 100% !important;
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
}

@media screen and (max-width: 768px) {
  .el-row {
    margin-left: 0 !important;
    margin-right: 0 !important;
  }
  
  .el-col {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
  
  .security-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .security-item .el-button {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    gap: 10px;
  }

  .card-header .el-button {
    width: 100%;
  }

  .el-dialog {
    width: 95% !important;
    max-width: 95% !important;
  }

  .el-form-item {
    margin-bottom: 15px;
  }
}
</style> 