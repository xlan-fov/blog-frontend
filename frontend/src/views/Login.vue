<template>
  <div class="login-container">
    <div class="login-card">
      <div class="card-header">
        <h2>登录</h2>
      </div>
      
      <div class="login-tabs">
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'account' }" 
          @click="activeTab = 'account'"
        >
          账号密码登录
        </div>
        <div 
          class="tab-item" 
          :class="{ active: activeTab === 'phone' }" 
          @click="activeTab = 'phone'"
        >
          手机验证码登录
        </div>
      </div>
      
      <!-- 账号密码登录 -->
      <div v-show="activeTab === 'account'" class="tab-content">
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">👤</i>
          </div>
          <input 
            v-model="credentials.username" 
            placeholder="请输入账号"
            class="login-input" 
          />
        </div>
        
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">🔒</i>
          </div>
          <input 
            type="password" 
            v-model="credentials.password" 
            placeholder="请输入密码"
            class="login-input" 
          />
        </div>
        
        <div v-if="showSlider" class="form-item">
          <div class="slider-container">
            <input 
              type="range" 
              v-model="sliderValue" 
              min="0" 
              max="100" 
              class="slider"
              @change="handleSliderChange"
            />
            <div class="slider-text">{{ formatTooltip(sliderValue) }}</div>
          </div>
        </div>

        <button 
          @click="onLogin" 
          class="login-btn"
        >立即登录</button>
      </div>
      
      <!-- 手机验证码登录 -->
      <div v-show="activeTab === 'phone'" class="tab-content">
        <div class="form-item">
          <div class="input-prefix">
            <i class="input-icon">📱</i>
          </div>
          <input 
            v-model="phoneForm.phone" 
            placeholder="请输入手机号"
            class="login-input" 
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
              class="login-input code-input" 
            />
            <button 
              class="code-btn" 
              @click="sendCode"
              :disabled="countdown > 0"
            >
              {{ countdown > 0 ? `${countdown}s后重试` : '获取验证码' }}
            </button>
          </div>
        </div>
        
        <button 
          @click="handlePhoneLogin" 
          class="login-btn"
        >立即登录</button>
      </div>
      
      <div class="login-footer">
        <router-link to="/register" class="register-link">注册账号</router-link>
      </div>
      
      <!-- 开发环境下的测试按钮 -->
      <button 
        v-if="isDev" 
        @click="testLogin" 
        class="test-btn"
      >模拟登录成功</button>
    </div>
    
    <!-- 如果需要验证码，在需要时才显示 -->
    <div v-if="requireCaptcha" class="captcha-container">
      <SliderCaptcha v-model="captchaPassed" />
    </div>
  </div>
</template>

<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, reactive, onMounted } from 'vue'
// 导入登录API
import { login } from '@/api/user'
// 导入滑块验证码组件
import SliderCaptcha from '@/components/SliderCaptcha.vue'
// 导入路由器用于页面跳转
import { useRouter } from 'vue-router'

// 获取路由器实例
const router = useRouter()

// 判断是否为开发环境
const isDev = ref(process.env.NODE_ENV === 'development')

// 创建响应式的登录凭证对象
const credentials = reactive({ username:'', password:'', smsCode:'' })
// 手机登录表单
const phoneForm = reactive({ phone: '', code: '' })
// 当前活动标签页
const activeTab = ref('account')
// 登录失败次数计数器
const failedCount = ref(0)
// 是否需要验证码标志
const requireCaptcha = ref(false)
// 验证码是否通过标志
const captchaPassed = ref(false)
// 滑块验证
const showSlider = ref(false)
const sliderValue = ref(0)
// 验证码倒计时
const countdown = ref(0)

// 组件挂载时执行的函数
onMounted(() => {
  // 从本地存储读取登录失败次数
  const storedFailedCount = localStorage.getItem('loginFailedCount')
  if (storedFailedCount) {
    failedCount.value = parseInt(storedFailedCount)
    // 如果失败次数超过3次，显示验证码
    if (failedCount.value >= 3) {
      showSlider.value = true
    }
  }
})

// 格式化滑块提示
const formatTooltip = (val) => {
  return `${val}%`
}

// 滑块变化处理
const handleSliderChange = (event) => {
  if (sliderValue.value === 100) {
    onLogin()
  }
}

// 登录处理函数
async function onLogin() {
  try {
    // 如果超过3次且未通过验证码，则阻止登录
    if (failedCount.value >= 3 && sliderValue.value !== 100) {
      alert('请完成滑块验证')
      return
    }
    
    // 调用登录接口
    const res = await login(credentials)
    
    // 登录成功，保存token和用户角色到本地
    localStorage.setItem('token', res.data.token)
    const userRole = res.data.role || 'user' // 默认为普通用户
    localStorage.setItem('userRole', userRole)
    
    // 重置失败次数
    failedCount.value = 0
    localStorage.setItem('loginFailedCount', '0')
    
    // 根据用户角色跳转到不同页面
    if (userRole === 'admin') {
      // 管理员跳转到文章管理页面
      router.push({ name: 'AdminArticleList' })
    } else {
      // 普通用户跳转到博客列表页面
      router.push('/blog')
    }
  } catch (error) {
    // 登录失败处理
    failedCount.value++
    localStorage.setItem('loginFailedCount', failedCount.value.toString())
    
    // 超过3次失败，显示验证码
    if (failedCount.value >= 3) {
      showSlider.value = true
    }
    
    alert('登录失败，请检查用户名和密码')
  }
}

// 手机登录
function handlePhoneLogin() {
  if (!phoneForm.phone || !phoneForm.code) {
    alert('请输入手机号和验证码')
    return
  }
  
  // 模拟手机登录成功
  localStorage.setItem('token', 'phone-login-token')
  localStorage.setItem('userRole', 'user')
  router.push('/blog')
}

// 发送验证码
function sendCode() {
  if (!phoneForm.phone) {
    alert('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    alert('请输入正确的手机号')
    return
  }
  
  // 倒计时开始
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
  
  alert('验证码已发送')
}

// 测试登录功能（仅开发环境使用）
function testLogin() {
  // 弹出选择框，选择以管理员或普通用户身份登录
  const isAdmin = confirm('是否以管理员身份登录？\n点击"确定"以管理员身份登录，点击"取消"以普通用户身份登录')
  
  // 设置模拟的token和用户角色
  localStorage.setItem('token', 'test-token-for-development')
  localStorage.setItem('userRole', isAdmin ? 'admin' : 'user')
  
  // 根据选择的角色跳转到不同页面
  if (isAdmin) {
    router.push({ name: 'AdminArticleList' })
  } else {
    router.push('/blog')
  }
}
</script>

<style scoped>
.login-container {
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

.login-card {
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

.login-tabs {
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
  color: #4e6ef2;
  font-weight: 500;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 2px;
  background-color: #4e6ef2;
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
  top: 50%;
  transform: translateY(-50%);
  color: #999;
}

.input-icon {
  font-style: normal;
}

.login-input {
  width: 100%;
  height: 46px;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 0 12px 0 40px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.login-input:focus {
  border-color: #4e6ef2;
  outline: none;
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

.login-btn {
  width: 100%;
  height: 46px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 10px;
}

.login-btn:hover {
  background-color: #4662d9;
}

.login-footer {
  margin-top: 20px;
  text-align: center;
}

.register-link {
  color: #4e6ef2;
  text-decoration: none;
  font-size: 14px;
}

.register-link:hover {
  text-decoration: underline;
}

.test-btn {
  width: 100%;
  height: 40px;
  background-color: #ff9800;
  color: white;
  border: none;
  border-radius: 8px;
  margin-top: 15px;
  font-size: 14px;
  cursor: pointer;
}

.captcha-container {
  width: 360px;
  margin-top: 20px;
}

.slider-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.slider {
  width: 100%;
  height: 20px;
}

.slider-text {
  margin-top: 10px;
  color: #666;
}

/* 移动设备响应式设计 */
@media screen and (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 20px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
  
  .captcha-container {
    width: 90%;
  }
}
</style>
