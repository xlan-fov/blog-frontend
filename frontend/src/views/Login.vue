<template>
  <div class="login-container">
    <h1 class="system-title">XXX 管理系统</h1>
    
    <div class="login-box">
      <input 
        v-model="credentials.username" 
        placeholder="请输入账号"
        class="login-input" 
      />
      
      <input 
        type="password" 
        v-model="credentials.password" 
        placeholder="请输入密码"
        class="login-input" 
      />
      
      <div class="buttons-row">
        <button 
          @click="onLogin" 
          class="login-btn"
        >立即登录</button>
        
        <router-link 
          to="/register" 
          class="register-btn"
        >注册</router-link>
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
// 登录失败次数计数器
const failedCount = ref(0)
// 是否需要验证码标志
const requireCaptcha = ref(false)
// 验证码是否通过标志
const captchaPassed = ref(false)

// 组件挂载时执行的函数
onMounted(() => {
  // ...读取本地失败次数...
})

// 登录处理函数
async function onLogin() {
  try {
    // 如果超过3次且未通过验证码，则阻止登录
    if (failedCount.value >= 3 && !captchaPassed.value) {
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
      // 普通用户跳转到内容管理页面
      router.push({ name: 'UserContentManage' })
    }
  } catch (error) {
    // 登录失败处理
    failedCount.value++
    localStorage.setItem('loginFailedCount', failedCount.value.toString())
    
    // 超过3次失败，显示验证码
    if (failedCount.value >= 3) {
      requireCaptcha.value = true
    }
    
    alert('登录失败，请检查用户名和密码')
  }
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
    router.push({ name: 'UserContentManage' })
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
  
  /* 固定位置在视口中心 */
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

.system-title {
  font-size: 24px;
  font-weight: normal;
  color: #333;
  margin-bottom: 30px;
}

.login-box {
  width: 360px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.login-input {
  width: 100%;
  height: 46px;
  margin-bottom: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 0 12px;
  font-size: 16px;
  box-sizing: border-box;
  text-align: center;
}

.buttons-row {
  display: flex;
  gap: 20px;
  margin-top: 10px;
}

.login-btn {
  flex: 1;
  height: 46px;
  background-color: #4e6ef2;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.login-btn:hover {
  background-color: #4662d9;
}

.register-btn {
  flex: 1;
  height: 46px;
  background-color: #f0f0f0;
  color: #333;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  text-decoration: none;
  transition: background-color 0.2s;
}

.register-btn:hover {
  background-color: #e5e5e5;
}

.test-btn {
  width: 100%;
  height: 40px;
  background-color: #ff9800;
  color: white;
  border: none;
  border-radius: 4px;
  margin-top: 15px;
  font-size: 14px;
  cursor: pointer;
}

.captcha-container {
  width: 360px;
  margin-top: 20px;
}
</style>
