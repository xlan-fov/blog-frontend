<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>管理员登录</h2>
      </template>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入管理员账号">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password>
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="authCode">
          <el-input v-model="loginForm.authCode" placeholder="请输入管理员授权码">
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleLoginClick" :loading="loading" style="width: 100%">
            管理员登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          show-icon
          :closable="false"
        />
        <p class="warning-text">注意：本页面仅供系统管理员使用，未授权访问将被记录</p>
      </div>
    </el-card>

    <!-- 滑块验证弹窗 -->
    <el-dialog
      v-model="showSliderDialog"
      title="安全验证"
      width="380px"
      :show-close="false"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <SlideVerify
        ref="slideVerifyRef"
        @success="handleSlideSuccess"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import SlideVerify from '@/components/SlideVerify.vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const errorMessage = ref('')
const loginFormRef = ref(null)
const showSliderDialog = ref(false)
const slideVerified = ref(false)
const slideVerifyRef = ref(null)

const loginForm = reactive({
  username: '',
  password: '',
  authCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  authCode: [{ required: true, message: '请输入管理员授权码', trigger: 'blur' }]
}

const handleLoginClick = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    if (userStore.userInfo.loginAttempts >= 3) {
      showSliderDialog.value = true
      return
    }

    await handleLogin()
  })
}

const handleSlideSuccess = () => {
  showSliderDialog.value = false
  slideVerified.value = true
  handleLogin()
}

const resetSlideVerify = () => {
  slideVerified.value = false
  showSliderDialog.value = false
  if (slideVerifyRef.value) {
    slideVerifyRef.value.reset()
  }
}

const handleLogin = async () => {
  if (userStore.userInfo.loginAttempts >= 3 && !slideVerified.value) {
    ElMessage.error('请完成安全验证')
    showSliderDialog.value = true
    return
  }

  loading.value = true
  try {
    const result = await userStore.adminLogin(loginForm.username, loginForm.password, loginForm.authCode)
    
    if (result === true) {
      ElMessage.success('登录成功')
      const redirectPath = router.currentRoute.value.query.redirect || '/admin/blog-management'
      router.push(redirectPath)
    } else {
      ElMessage.error('用户名、密码或授权码错误')
      if (userStore.userInfo.loginAttempts >= 3) {
        recordAbnormalLogin(loginForm.username)
      }
      resetSlideVerify()
    }
  } catch (error) {
    ElMessage.error('登录失败')
    resetSlideVerify()
  } finally {
    loading.value = false
  }
}

const recordAbnormalLogin = (username) => {
  console.log('记录异常登录:', new Date().toISOString(), '用户名:', username)
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/login_bg.jpeg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.login-card {
  width: 480px;
  min-height: 480px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
}

.login-footer {
  text-align: center;
  margin-top: auto;
  padding: 20px 24px;
  position: relative;
}

.warning-text {
  font-size: 12px;
  color: #E6A23C;
  text-align: center;
  margin-top: 16px;
}

:deep(.el-card__header) {
  text-align: center;
  padding: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header h2) {
  margin: 0;
  font-size: 32px;
  font-weight: 800;
  letter-spacing: 4px;
  background: linear-gradient(90deg, #409EFF 10%, #66b1ff 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(64,158,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.95);
  height: 40px;
}

:deep(.el-button) {
  height: 40px;
  font-size: 16px;
}

:deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
}

/* 平板设备 */
@media screen and (max-width: 1024px) {
  .login-card {
    width: 420px;
    min-height: 450px;
  }
}

/* 移动设备 */
@media screen and (max-width: 768px) {
  .login-container {
    background-image: url('https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/login_bg_mobile.png');
    background-position: center 30%;
  }

  .login-card {
    width: 90%;
    max-width: 360px;
    min-height: 420px;
    margin: 20px;
  }

  :deep(.el-card__header h2) {
    font-size: 24px;
  }

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-button) {
    height: 40px;
    font-size: 14px;
    line-height: 40px;
  }

  :deep(.el-input__wrapper) {
    height: 40px;
    line-height: 40px;
  }

  :deep(.el-input__inner) {
    height: 40px;
    line-height: 40px;
  }
}

/* 小屏移动设备 */
@media screen and (max-width: 375px) {
  .login-card {
    min-height: 380px;
  }

  :deep(.el-card__header) {
    padding: 16px;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

:deep(.el-dialog__header) {
  text-align: center;
  margin-right: 0;
  padding: 20px;
}

:deep(.el-dialog__headerbtn) {
  display: none;
}

:deep(.el-dialog__body) {
  padding: 0 20px 30px;
}
</style> 