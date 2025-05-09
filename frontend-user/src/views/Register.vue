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
                <Captcha v-model="captchaText" ref="captchaRef" />
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

            <el-form-item prop="password">
              <el-input v-model="phoneForm.password" type="password" placeholder="请输入密码" show-password>
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
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

const router = useRouter()
const activeTab = ref('account')
const loading = ref(false)
const countdown = ref(0)
const registerFormRef = ref(null)
const phoneFormRef = ref(null)
const captchaRef = ref(null)
const captchaText = ref('')

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})

const phoneForm = reactive({
  phone: '',
  code: '',
  password: ''
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
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    // 先验证表单
    await registerFormRef.value.validate()
    
    // TODO: 验证码校验功能（测试阶段暂时注释）
    /*
    // 验证验证码
    if (!registerForm.captcha) {
      ElMessage.error('请输入验证码')
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
          captcha: registerForm.captcha
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

    // TODO: 替换为axios请求
    // 任务：实现账号密码注册的axios接口请求
    // 验证通过，继续注册流程
    if (registerForm.username === 'admin') {
      ElMessage.error('用户名已存在')
      return
    }
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
    // TODO: 验证码刷新功能（测试阶段暂时注释）
    // captchaRef.value?.refreshCaptcha()
  }
}

const handlePhoneRegister = async () => {
  try {
    // 先验证表单
    await phoneFormRef.value.validate()
    
    // TODO: 验证码校验功能（测试阶段暂时注释）
    /*
    // 验证验证码
    if (!phoneForm.captcha) {
      ElMessage.error('请输入验证码')
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
          captcha: phoneForm.captcha
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

    // TODO: 替换为axios请求
    // 任务：实现手机验证码注册的axios接口请求
    // 验证通过，继续注册流程
    if (phoneForm.phone === '13800138000') {
      ElMessage.error('手机号已被注册')
      return
    }
    ElMessage.success('注册成功')
    router.push('/login')
  } catch (error) {
    ElMessage.error('请检查表单填写是否正确')
    // TODO: 验证码刷新功能（测试阶段暂时注释）
    // captchaRef.value?.refreshCaptcha()
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

const captchaUrl = ref('')

const refreshCaptcha = () => {
  // 添加时间戳防止缓存
  captchaUrl.value = `/api/captcha?t=${Date.now()}`
}

onMounted(() => {
  refreshCaptcha()
})

defineExpose({
  refreshCaptcha
})
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/register_bg.jpeg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.register-card {
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

.register-footer {
  text-align: right;
  margin-top: auto;
  padding: 20px 24px;
  position: relative;
}

.register-footer a {
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

.register-footer a::before {
  content: '←';
  font-size: 16px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.register-footer a:hover {
  color: #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
}

.register-footer a:hover::before {
  opacity: 1;
  transform: translateX(-2px);
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
  text-fill-color: transparent;
  text-shadow: 0 2px 8px rgba(64,158,255,0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

:deep(.el-card__header h2)::before {
  content: '\f234';
  font-family: 'Font Awesome 5 Free', 'FontAwesome';
  font-weight: 900;
  font-size: 28px;
  color: #67C23A;
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

/* 平板设备 */
@media screen and (max-width: 1024px) {
  .register-card {
    width: 420px;
    min-height: 540px;
  }
}

/* 移动设备 */
@media screen and (max-width: 768px) {
  .register-container {
    background-image: url('https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/register_bg_mobile.png');
    background-position: center 30%;
  }

  .register-card {
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
  .register-card {
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