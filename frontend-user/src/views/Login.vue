<!--
重要接口：
handleLogin()
handlePhoneLogin()
-->
<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2>登录</h2>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号密码登录" name="account">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名">
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

            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input v-model="loginForm.captcha" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <Captcha v-model="captchaText" ref="captchaRef" :onCaptchaChange="handleCaptchaChange" />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机验证码登录" name="phone">
          <el-form :model="phoneForm" :rules="phoneRules" ref="phoneFormRef">
            <el-form-item prop="phone">
              <el-input v-model="phoneForm.phone" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="code">
              <div class="code-input">
                <el-input v-model="phoneForm.code" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
                <el-button type="primary" @click="sendCode" :disabled="countdown > 0">
                  {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handlePhoneLogin" :loading="loading" style="width: 100%">
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="login-footer">
        <router-link to="/register">没有账号？立即注册</router-link>
        <router-link to="/senti-admin-auth">管理员登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, Key } from '@element-plus/icons-vue'
import Captcha from '@/components/Captcha.vue'
import { loginByUsername, loginByPhone, sendPhoneCode } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('account')
const loading = ref(false)
const countdown = ref(0)
const loginFormRef = ref(null)
const phoneFormRef = ref(null)
const captchaRef = ref(null)
const captchaText = ref('')
const captchaId = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
})

const phoneForm = reactive({
  phone: '',
  code: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const handleCaptchaChange = (newCaptchaId) => {
  captchaId.value = newCaptchaId
  localStorage.setItem('captchaId', newCaptchaId)
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    
    if (!loginForm.captcha) {
      ElMessage.error('请输入验证码')
      return
    }
    
    loading.value = true
    
    // 不要在登录前清理token，只有在明确需要重新登录时才清理
    
    try {
      const result = await loginByUsername({
        username: loginForm.username,
        password: loginForm.password,
        captcha: loginForm.captcha,
        captchaId: captchaId.value
      })
      
      if (result.code === 200 || result.code === 0) {
        // 登录成功后设置新的用户信息和token
        userStore.setUserInfo(result.data)
        handleLoginSuccess()
      } else {
        ElMessage.error(result.msg || '登录失败')
        // 只在登录失败时刷新验证码
        if (captchaRef.value) {
          captchaRef.value.refreshCaptcha()
        }
        loginForm.captcha = ''
      }
    } catch (error) {
      console.error('登录请求失败:', error)
      // 只在请求失败时刷新验证码
      if (captchaRef.value) {
        captchaRef.value.refreshCaptcha()
      }
      loginForm.captcha = ''
    } finally {
      loading.value = false
    }
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
    if (captchaRef.value) {
      captchaRef.value.refreshCaptcha()
    }
  }
}

const handlePhoneLogin = async () => {
  try {
    await phoneFormRef.value.validate()
    
    loading.value = true
    
    try {
      const result = await loginByPhone({
        phone: phoneForm.phone,
        code: phoneForm.code
      })
      
      if (result.code === 200 || result.code === 0) {
        userStore.setUserInfo(result.data)
        ElMessage.success('登录成功')
        router.push('/')
      } else {
        ElMessage.error(result.msg || '登录失败')
      }
    } catch (error) {
      console.error('手机登录失败:', error)
    } finally {
      loading.value = false
    }
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
  }
}

const sendCode = async () => {
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.error('请输入正确的手机号')
    return
  }

  try {
    await sendPhoneCode(phoneForm.phone)

    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    ElMessage.success('验证码已发送')
  } catch (error) {
    // 错误处理已经在API层完成
  }
}

onMounted(() => {
  // 初始化验证码
  if (captchaRef.value) {
    captchaRef.value.refreshCaptcha()
  }
})

// 登录成功后的处理
const handleLoginSuccess = () => {
  ElMessage.success('登录成功')
  const redirectPath = router.currentRoute.value.query.redirect
  if (redirectPath) {
    router.push(redirectPath)
  } else {
    router.push('/dashboard')
  }
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
  min-height: 520px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding: 0 24px 20px;
}

.login-footer a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.login-footer a:hover {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.code-input {
  display: flex;
  gap: 10px;
  align-items: center;
}

.code-input .el-input {
  flex: 1;
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-wrapper .el-input {
  flex: 1;
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
  background: linear-gradient(90deg, #67C23A 10%, #409EFF 90%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
}

:deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
  font-size: 16px;
  height: 48px;
  line-height: 48px;
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
  line-height: 40px;
}

:deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
}

@media screen and (max-width: 768px) {
  .login-card {
    width: 90%;
    max-width: 360px;
    min-height: 480px;
    margin: 20px;
  }
}
</style>