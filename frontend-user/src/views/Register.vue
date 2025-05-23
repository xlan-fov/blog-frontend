<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <h2>注册</h2>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="账号密码注册" name="account">
          <el-form :model="registerForm" :rules="rules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名">
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input v-model="registerForm.captcha" placeholder="请输入验证码">
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <Captcha v-model="captchaText" ref="captchaRef" :onCaptchaChange="handleCaptchaChange" />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="手机验证码注册" name="phone">
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

            <el-form-item prop="captcha">
              <div class="captcha-wrapper">
                <el-input v-model="phoneForm.captcha" placeholder="请输入图片验证码">
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <Captcha v-model="captchaText" ref="captchaRef" :onCaptchaChange="handleCaptchaChange" />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handlePhoneRegister" :loading="loading" style="width: 100%">
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="register-footer">
        <router-link to="/login">已有账号？立即登录</router-link>
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
import { registerByUsername, registerByPhone, sendPhoneCode } from '@/api/user'

const router = useRouter()
const activeTab = ref('account')
const loading = ref(false)
const countdown = ref(0)
const registerFormRef = ref(null)
const phoneFormRef = ref(null)
const captchaRef = ref(null)
const captchaText = ref('')
const captchaId = ref('')

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const phoneForm = reactive({
  phone: '',
  code: '',
  captcha: ''
})

const validatePassword = (rule, value, callback) => {
  const patterns = [/[A-Z]/, /[a-z]/, /[0-9]/, /[^A-Za-z0-9]/]
  const typesMatched = patterns.filter(r => r.test(value)).length
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 8) {
    callback(new Error('密码长度不能小于8位'))
  } else if (typesMatched < 3) {
    callback(new Error('密码至少包含数字、大写字母、小写字母、特殊字符中的三种'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 6, max: 20, message: '用户名长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
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
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入图片验证码', trigger: 'blur' }
  ]
}

const handleCaptchaChange = (newCaptchaId) => {
  captchaId.value = newCaptchaId
  localStorage.setItem('captchaId', newCaptchaId)
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    
    if (!registerForm.captcha) {
      ElMessage.error('请输入验证码')
      return
    }
    
    loading.value = true
    
    try {
      const result = await registerByUsername({
        username: registerForm.username,
        password: registerForm.password,
        captcha: registerForm.captcha,
        captchaId: captchaId.value
      })
      
      if (result.code === 200 || result.code === 0) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(result.msg || '注册失败')
        if (captchaRef.value) {
          captchaRef.value.refreshCaptcha()
        }
        registerForm.captcha = ''
      }
    } catch (error) {
      console.error('注册请求失败:', error)
      if (captchaRef.value) {
        captchaRef.value.refreshCaptcha()
      }
      registerForm.captcha = ''
    } finally {
      loading.value = false
    }
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
  }
}

const handlePhoneRegister = async () => {
  try {
    await phoneFormRef.value.validate()
    
    loading.value = true
    
    try {
      const result = await registerByPhone({
        phone: phoneForm.phone,
        code: phoneForm.code,
        captcha: phoneForm.captcha,
        captchaId: captchaId.value
      })
      
      if (result.code === 200 || result.code === 0) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(result.msg || '注册失败')
        if (captchaRef.value) {
          captchaRef.value.refreshCaptcha()
        }
        phoneForm.captcha = ''
      }
    } catch (error) {
      console.error('手机注册失败:', error)
      if (captchaRef.value) {
        captchaRef.value.refreshCaptcha()
      }
      phoneForm.captcha = ''
    } finally {
      loading.value = false
    }
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
  }
}

const sendCode = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  if (!phoneForm.captcha) {
    ElMessage.warning('请先输入图片验证码')
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
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/login_bg.jpeg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.register-card {
  width: 480px;
  min-height: 600px;
  background-color: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.register-footer {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 0 24px 20px;
}

.register-footer a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.register-footer a:hover {
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
  .register-card {
    width: 90%;
    max-width: 360px;
    min-height: 560px;
    margin: 20px;
  }
}
</style>