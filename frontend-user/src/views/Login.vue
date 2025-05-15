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
                <Captcha v-model="captchaText" ref="captchaRef" />
              </div>
            </el-form-item>

            <el-slider
              v-if="showSlider"
              v-model="sliderValue"
              :min="0"
              :max="100"
              :format-tooltip="formatTooltip"
              @change="handleSliderChange"
            />

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
        <router-link to="/register">注册账号</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Phone, Message, Key } from '@element-plus/icons-vue'
import Captcha from '@/components/Captcha.vue'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('account')
const loading = ref(false)
const showSlider = ref(false)
const sliderValue = ref(0)
const countdown = ref(0)
const captchaRef = ref(null)
const captchaText = ref('')

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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const formatTooltip = (val) => {
  return `${val}%`
}

const handleSliderChange = (val) => {
  if (val === 100) {
    handleLogin()
  }
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // TODO: 验证码校验功能（测试阶段暂时注释）
  /*
  if (!loginForm.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }

  // TODO: 替换为axios请求
  // 任务：将fetch请求替换为axios请求，统一接口调用方式
  try {
    const response = await fetch('/api/verify-captcha', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        captcha: loginForm.captcha
      })
    })
    const data = await response.json()
    if (!data.success) {
      ElMessage.error('验证码错误')
      captchaRef.value?.refreshCaptcha()
      return
    }
  } catch (error) {
    ElMessage.error('验证码验证失败')
    return
  }
  */

  if (userStore.userInfo.loginAttempts >= 3) {
    showSlider.value = true
    if (sliderValue.value !== 100) {
      ElMessage.error('请完成滑块验证')
      return
    }
  }

  loading.value = true
  try {
    // TODO: 替换为axios请求
    // 任务：将userStore.login方法内部实现改为使用axios请求
    const result = await userStore.login(loginForm.username, loginForm.password)
    
    if (result === 'banned') {
      ElMessage.error('您的账号已被封禁，请联系管理员')
      loading.value = false
      return
    }
    
    if (result === true) {
      ElMessage.success('登录成功')
      // 获取重定向路径
      const redirectPath = router.currentRoute.value.query.redirect || '/'
      router.push(redirectPath)
    } else {
      ElMessage.error('用户名或密码错误')
      // 登录失败，增加失败次数计数
      if (userStore.userInfo.loginAttempts >= 3) {
        // 记录异常登录信息，供管理员查看
        recordAbnormalLogin(loginForm.username)
      }
      // TODO: 验证码刷新功能（测试阶段暂时注释）
      // captchaRef.value?.refreshCaptcha()
    }
  } catch (error) {
    ElMessage.error('登录失败')
    // TODO: 验证码刷新功能（测试阶段暂时注释）
    // captchaRef.value?.refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 记录异常登录信息
const recordAbnormalLogin = (username) => {
  // TODO: 替换为axios请求，向后端发送异常登录记录
  console.log('记录异常登录:', username, new Date().toISOString())
}

const handlePhoneLogin = () => {
  // TODO: 替换为axios请求
  // 任务：实现手机验证码登录的axios接口请求
  // 模拟手机验证码登录
  if (phoneForm.phone === '13800138000' && phoneForm.code === '123456') {
    ElMessage.success('登录成功')
    router.push('/blog')
  } else {
    ElMessage.error('手机号或验证码错误')
  }
}

const sendCode = () => {
  // TODO: 替换为axios请求
  // 任务：实现发送手机验证码的axios接口请求
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  ElMessage.success('验证码已发送')
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
  min-height: 580px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
}

.login-footer {
  text-align: right;
  margin-top: auto;
  padding: 20px 24px;
  position: relative;
}

.login-footer a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background-color: rgba(64, 158, 255, 0);
}

.login-footer a::before {
  content: '→';
  font-size: 16px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.login-footer a:hover {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.login-footer a:hover::before {
  opacity: 1;
  transform: translateX(2px);
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

/* 可选：加一个用户图标 */
:deep(.el-card__header h2)::before {
  content: '\f007';
  font-family: 'Font Awesome 5 Free', 'FontAwesome';
  font-weight: 900;
  font-size: 28px;
  color: #409EFF;
  margin-right: 8px;
  display: inline-block;
  vertical-align: middle;
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
  font-size: 16px;
}

:deep(.el-input__inner) {
  height: 40px;
  line-height: 40px;
}

:deep(.el-slider) {
  margin: 20px 0;
}

/* 平板设备 */
@media screen and (max-width: 1024px) {
  .login-card {
    width: 420px;
    min-height: 540px;
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
    min-height: 520px;
    margin: 20px;
  }

  :deep(.el-card__header h2) {
    font-size: 24px;
  }

  :deep(.el-tabs__item) {
    font-size: 14px;
    height: 40px;
    line-height: 40px;
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

  .code-input {
    align-items: center;
  }
}

/* 小屏移动设备 */
@media screen and (max-width: 375px) {
  .login-card {
    min-height: 480px;
  }

  :deep(.el-card__header) {
    padding: 16px;
  }

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}
</style> 