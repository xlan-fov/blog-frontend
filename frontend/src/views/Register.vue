<template>
  <div class="register-container">
    <div class="register-card">
      <div class="card-header">
        <h2>注册</h2>
      </div>
      
      <div class="register-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'account' }" 
          @click="activeTab = 'account'"
        >
          账号密码注册
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'phone' }" 
          @click="activeTab = 'phone'"
        >
          手机验证码注册
        </div>
      </div>
      
      <!-- 账号密码注册 -->
      <div v-show="activeTab === 'account'" class="tab-content">
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">👤</i>
          </div>
          <input 
            v-model="form.username" 
            placeholder="请输入用户名（3-20字符）"
            class="register-input" 
            @blur="checkName"
          />
          <div v-if="errors.username" class="input-error">{{ errors.username }}</div>
        </div>
        
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">🔒</i>
          </div>
          <input 
            type="password" 
            v-model="form.password" 
            placeholder="请输入密码（8位以上且包含字母数字）"
            class="register-input" 
          />
          <div v-if="errors.password" class="input-error">{{ errors.password }}</div>
        </div>
        
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">🔒</i>
          </div>
          <input 
            type="password" 
            v-model="form.confirmPassword" 
            placeholder="请确认密码"
            class="register-input" 
          />
          <div v-if="errors.confirmPassword" class="input-error">{{ errors.confirmPassword }}</div>
        </div>
        
        <div class="form-item captcha-wrapper">
          <div class="input-prefix">
            <i class="input-icon">🔑</i>
          </div>
          <input 
            v-model="form.captcha"
            placeholder="请输入验证码"
            class="register-input" 
          />
          <div class="captcha-image">
            <img src="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAiIGhlaWdodD0iNDAiIHZpZXdCb3g9IjAgMCAxMjAgNDAiPjxyZWN0IHdpZHRoPSIxMjAiIGhlaWdodD0iNDAiIGZpbGw9IiNmMGYyZjUiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1mYW1pbHk9IkFyaWFsLCBzYW5zLXNlcmlmIiBmb250LXNpemU9IjE4IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiBmaWxsPSIjMzMzIj5DTElDSzwvdGV4dD48L3N2Zz4=" 
                 alt="验证码" 
                 @click="refreshCaptcha" />
          </div>
        </div>
        
        <button @click="onSubmit" class="register-btn">注册</button>
      </div>
      
      <!-- 手机验证码注册 -->
      <div v-show="activeTab === 'phone'" class="tab-content">
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">📱</i>
          </div>
          <input 
            v-model="phoneForm.phone" 
            placeholder="请输入手机号"
            class="register-input" 
          />
        </div>
        
        <div class="form-item">
          <div class="code-input-wrapper">
            <div class="input-prefix">
              <i class="input-icon">🔑</i>
            </div>
            <input 
              v-model="phoneForm.code" 
              placeholder="请输入验证码"
              class="register-input code-input" 
            />
            <button 
              class="code-btn" 
              @click="sendSms"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </button>
          </div>
        </div>
        
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">🔒</i>
          </div>
          <input 
            type="password" 
            v-model="phoneForm.password" 
            placeholder="请输入密码（8位以上且包含字母数字）"
            class="register-input" 
          />
        </div>
        
        <button @click="onPhoneSubmit" class="register-btn">注册</button>
      </div>
      
      <div class="register-footer">
        <router-link to="/login" class="login-link">已有账号？立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// 从Vue库导入响应式API
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
// 导入用户相关API函数
import { checkUsername, register } from '@/api/user'

const router = useRouter()
const activeTab = ref('account')
const captchaUrl = ref('')
const countdown = ref(0)

// reactive用于创建响应式对象，当数据变化时UI会自动更新
// 表单数据对象
const form = reactive({ 
  username:'', 
  password:'', 
  confirmPassword: '',
  captcha: ''
})

// 手机表单
const phoneForm = reactive({
  phone: '',
  code: '',
  password: ''
})

// 表单错误信息对象
const errors = reactive({ 
  username:'', 
  password:'', 
  confirmPassword: '' 
})

// 验证密码格式
function validatePassword(password) {
  // 至少8位，包含数字和字母
  const hasLetterAndNumber = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/
  return hasLetterAndNumber.test(password)
}

// 异步函数，检查用户名是否可用
async function checkName() {
  if (!form.username) {
    errors.username = '用户名不能为空'
    return
  }
  
  if (form.username.length < 3 || form.username.length > 20) {
    errors.username = '用户名长度需要在3-20之间'
    return
  }
  
  errors.username = ''
  
  try {
    await checkUsername(form.username)
    // 如果没有抛出异常，说明用户名可用
  } catch (error) {
    errors.username = '用户名已被占用'
  }
}

// 刷新验证码
function refreshCaptcha() {
  captchaUrl.value = `/api/captcha?t=${Date.now()}`
}

// 发送短信验证码函数
function sendSms() {
  if (!phoneForm.phone) {
    alert('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    alert('请输入有效的手机号')
    return
  }
  
  // 开始倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  alert('验证码已发送')
}

// 表单提交处理函数
async function onSubmit() {
  // 验证用户名
  if (!form.username) {
    errors.username = '请输入用户名'
    return
  }
  
  // 验证密码
  if (!form.password) {
    errors.password = '请输入密码'
    return
  }
  
  if (!validatePassword(form.password)) {
    errors.password = '密码至少8位，且必须包含字母和数字'
    return
  }
  
  // 验证确认密码
  if (form.password !== form.confirmPassword) {
    errors.confirmPassword = '两次输入的密码不一致'
    return
  }
  
  // 验证验证码
  if (!form.captcha) {
    alert('请输入验证码')
    return
  }
  
  try {
    // 调用注册API
    await register({
      username: form.username,
      password: form.password
    })
    
    alert('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    alert('注册失败: ' + (error.message || '未知错误'))
    refreshCaptcha()
  }
}

// 手机表单提交
async function onPhoneSubmit() {
  if (!phoneForm.phone) {
    alert('请输入手机号')
    return
  }
  
  if (!phoneForm.code) {
    alert('请输入验证码')
    return
  }
  
  if (!phoneForm.password) {
    alert('请输入密码')
    return
  }
  
  if (!validatePassword(phoneForm.password)) {
    alert('密码至少8位，且必须包含字母和数字')
    return
  }
  
  try {
    // 调用注册API
    await register({
      phone: phoneForm.phone,
      code: phoneForm.code,
      password: phoneForm.password
    })
    
    alert('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    alert('注册失败: ' + (error.message || '未知错误'))
  }
}

onMounted(() => {
  refreshCaptcha()
})
</script>

<style scoped>
.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f5f5;
  background-image: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  
  /* 固定位置在视口中心 */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.register-card {
  width: 380px;
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  position: relative;
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.register-tabs {
  display: flex;
  margin-bottom: 25px;
  border-bottom: 1px solid #eee;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
  position: relative;
}

.tab-item.active {
  color: #67C23A;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 2px;
  background-color: #67C23A;
}

.tab-content {
  margin-bottom: 20px;
}

.form-item {
  margin-bottom: 20px;
  position: relative;
}

.input-prefix {
  position: absolute;
  left: 12px;
  top: 14px;
  color: #999;
}

.input-icon {
  font-style: normal;
}

.register-input {
  width: 100%;
  height: 46px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 12px 0 40px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.register-input:focus {
  border-color: #67C23A;
  outline: none;
}

.input-error {
  color: #F56C6C;
  font-size: 12px;
  margin-top: 5px;
}

.captcha-wrapper {
  display: flex;
  gap: 10px;
}

.captcha-image {
  width: 120px;
  height: 46px;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.code-btn {
  padding: 0 15px;
  height: 46px;
  background-color: #f5f7fa;
  border: 1px solid #ddd;
  border-radius: 8px;
  color: #333;
  cursor: pointer;
  white-space: nowrap;
}

.code-btn:disabled {
  background-color: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.register-btn {
  width: 100%;
  height: 46px;
  background-color: #67C23A;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 10px;
}

.register-btn:hover {
  background-color: #5daf34;
}

.register-footer {
  margin-top: 20px;
  text-align: center;
}

.login-link {
  color: #67C23A;
  text-decoration: none;
  font-size: 14px;
}

.login-link:hover {
  text-decoration: underline;
}

/* 移动设备响应式设计 */
@media screen and (max-width: 480px) {
  .register-card {
    width: 90%;
    padding: 20px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
}
</style>
