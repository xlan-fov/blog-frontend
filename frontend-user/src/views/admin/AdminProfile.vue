<template>
  <div class="admin-profile">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
        <el-card class="profile-card">
          <div class="profile-header">
            <div class="avatar-wrapper">
              <el-avatar :size="100" :src="adminProfile.avatar || userStore.userInfo.avatar" />
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
            <h2 class="username">{{ adminProfile.username }}</h2>
            <div class="role-tag">
              <el-tag type="danger">{{ adminProfile.role === 'admin' ? '超级管理员' : '管理员' }}</el-tag>
            </div>
          </div>
          
          <div class="profile-info">
            <div class="info-item">
              <div class="label">注册时间</div>
              <div class="value">{{ formatDateTime(adminProfile.createdAt) }}</div>
            </div>
            <div class="info-item">
              <div class="label">最后登录</div>
              <div class="value">{{ formatDateTime(adminProfile.lastLoginTime) }}</div>
            </div>
            <div class="info-item">
              <div class="label">管理权限</div>
              <div class="value">{{ adminProfile.role === 'admin' ? '全部' : '部分' }}</div>
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
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            
            
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" :disabled="!isEditing" />
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
              <el-button type="primary" @click="saveProfile" :loading="submitting">保存</el-button>
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
              <el-button @click="showLoginLogs = true">查看</el-button>
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
          <div>{{ maskPhone(profileForm.phone) }}</div>
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
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import adminApi from '@/api/admin'

const userStore = useUserStore()
const isEditing = ref(false)
const submitting = ref(false)
const countdown = ref(0)
const isLoading = ref(false)

// 管理员资料
const adminProfile = reactive({
  id: 0,
  username: '',
  phone: '',
  bio: '',
  role: '',
  avatar: '',
  createdAt: '',
  lastLoginTime: ''
})

// 个人资料表单
const profileForm = reactive({
  username: '',
  phone: '',
  bio: ''
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
const loginLogs = ref([
  {
    time: new Date().toLocaleString(),
    ip: '192.168.1.1',
    location: '北京',
    device: 'Windows Chrome',
    status: 'success'
  },
  {
    time: new Date(Date.now() - 24 * 60 * 60 * 1000).toLocaleString(),
    ip: '192.168.1.1',
    location: '北京',
    device: 'Windows Chrome',
    status: 'success'
  },
  {
    time: new Date(Date.now() - 48 * 60 * 60 * 1000).toLocaleString(),
    ip: '192.168.1.2',
    location: '上海',
    device: 'iOS Safari',
    status: 'success'
  },
  {
    time: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toLocaleString(),
    ip: '192.168.1.3',
    location: '广州',
    device: 'Android Chrome',
    status: 'failed'
  }
])

// 获取管理员资料
const fetchAdminProfile = async () => {
  isLoading.value = true
  try {
    const res = await adminApi.getAdminProfile()
    if (res.code === 200 && res.data) {
      // 更新管理员资料
      Object.assign(adminProfile, res.data)
      
      // 更新表单数据
      profileForm.username = res.data.username || ''
      profileForm.phone = res.data.phone || ''
      profileForm.bio = res.data.bio || ''
      
      // 更新用户存储
      userStore.updateProfile(res.data.avatar, res.data.bio)
    } else {
      ElMessage.error(res.message || '获取管理员资料失败')
    }
  } catch (error) {
    console.error('获取管理员资料失败:', error)
    ElMessage.error('获取管理员资料失败')
  } finally {
    isLoading.value = false
  }
}

// 保存个人资料
const saveProfile = async () => {
  // 验证手机号是否合法
  if (profileForm.phone) {
    const phoneRegex = /^1[3-9]\d{9}$/;
    if (!phoneRegex.test(profileForm.phone)) {
      ElMessage.error('请输入有效的手机号码');
      return;
    }
  }

  submitting.value = true
  try {
    const res = await adminApi.updateAdminProfile({
      phone: profileForm.phone,
      bio: profileForm.bio
    })
    
    if (res.code === 200) {
      // 更新本地数据
      adminProfile.phone = profileForm.phone
      adminProfile.bio = profileForm.bio
      
      // 更新用户存储
      userStore.updateProfile(adminProfile.avatar, profileForm.bio)
      
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
      const res = await adminApi.changePassword({
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
const sendCode = () => {
  if (!phoneForm.newPhone) {
    ElMessage.warning('请输入新手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.newPhone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  // TODO: 替换为axios请求
  // 模拟发送验证码
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  ElMessage.success('验证码已发送')
}

// 修改手机号
const changePhone = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      // TODO: 替换为axios请求
      // 模拟修改
      setTimeout(() => {
        profileForm.phone = phoneForm.newPhone
        showChangePhone.value = false
        ElMessage.success('手机号修改成功')
        // 重置表单
        phoneForm.newPhone = ''
        phoneForm.code = ''
        submitting.value = false
      }, 500)
    } catch (error) {
      ElMessage.error('手机号修改失败')
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
const uploadAvatar = (options) => {
  const file = options.file
  
  // 使用 FileReader 读取文件作为 base64 数据 URL
  const reader = new FileReader()
  reader.readAsDataURL(file)
  reader.onload = () => {
    // TODO: 替换为axios请求
    // 模拟上传
    setTimeout(() => {
      const avatarUrl = reader.result
      userStore.updateProfile(avatarUrl, userStore.userInfo.bio)
      ElMessage.success('头像上传成功')
    }, 500)
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '暂无记录'
  
  try {
    const date = new Date(dateTime)
    
    // 检查日期是否有效
    if (isNaN(date.getTime())) return dateTime
    
    // 格式化为 YYYY-MM-DD HH:MM:SS
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    }).replace(/\//g, '-')
  } catch (error) {
    console.error('日期格式化错误:', error)
    return dateTime // 出错时返回原始值
  }
}

// 手机号脱敏
const maskPhone = (phone) => {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 初始化
onMounted(() => {
  // 加载个人资料
  fetchAdminProfile()
})
</script>

<style scoped>
.admin-profile {
  padding: 16px;
}

.profile-card {
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
  border: 1px solid #e6e6e6;
}

.upload-icon {
  font-size: 18px;
  color: #606266;
}

.username {
  margin: 8px 0;
  font-size: 18px;
  font-weight: bold;
}

.role-tag {
  margin-bottom: 16px;
}

.profile-info {
  padding: 0 16px 16px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f5;
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  color: #909399;
}

.info-card {
  margin-bottom: 20px;
}

.security-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.security-items {
  padding: 0 16px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
}

.security-item:last-child {
  border-bottom: none;
}

.security-item-info .title {
  font-size: 16px;
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