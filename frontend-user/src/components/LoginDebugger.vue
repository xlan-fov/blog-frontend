<template>
  <div class="login-debugger">
    <h3>登录调试工具</h3>
    <div class="debug-section">
      <div>
        <label>用户名:</label>
        <input v-model="username" />
      </div>
      <div>
        <label>密码:</label>
        <input v-model="password" type="password" />
      </div>
      <button @click="debugLogin">测试登录</button>
    </div>
    <div v-if="token" class="token-info">
      <div>当前Token: {{ token }}</div>
      <button @click="clearToken">清除Token</button>
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const username = ref('user')
const password = ref('123456')
const token = ref('')
const errorMessage = ref('')

onMounted(() => {
  token.value = localStorage.getItem('token') || ''
})

const debugLogin = async () => {
  try {
    errorMessage.value = ''
    // 直接使用axios测试登录，绕过封装的API
    const res = await axios.post('/api/users/loginByname', {
      username: username.value,
      password: password.value
    })
    
    console.log('登录响应:', res.data)
    
    if (res.data.code === 200 && res.data.data && res.data.data.token) {
      token.value = res.data.data.token
      localStorage.setItem('token', token.value)
      alert('登录成功，已保存token')
    } else {
      errorMessage.value = res.data.message || '登录失败，无法获取token'
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    errorMessage.value = error.message || '登录请求失败'
  }
}

const clearToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userState')
  token.value = ''
  alert('已清除token和登录状态')
}
</script>

<style scoped>
.login-debugger {
  border: 1px solid #ddd;
  padding: 15px;
  margin: 15px;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.debug-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.debug-section div {
  display: flex;
  align-items: center;
}

.debug-section label {
  width: 80px;
}

button {
  margin-top: 10px;
  padding: 5px 10px;
  background: #4c84ff;
  color: white;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}

.token-info {
  margin-top: 15px;
  padding: 10px;
  background: #e6f7ff;
  border-radius: 3px;
  word-break: break-all;
}

.error-message {
  margin-top: 15px;
  padding: 10px;
  background: #fff1f0;
  color: #f5222d;
  border-radius: 3px;
}
</style>
