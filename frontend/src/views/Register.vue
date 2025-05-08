<template>
  <div class="auth-container">
    <h2>注册</h2>
    <form @submit.prevent="onSubmit">
      <!-- 用户名输入框 -->
      <input v-model="form.username" @blur="checkName" placeholder="用户名（6-20字符）" />
      <span v-if="errors.username">{{ errors.username }}</span>
      <!-- 密码输入框 -->
      <input type="password" v-model="form.password" placeholder="密码（6-16，含大小写+数字）" />
      <span v-if="errors.password">{{ errors.password }}</span>
      <!-- 手机号和验证码输入组件 -->
      <SmsInput v-model="form.phone" @send="sendSms" placeholder="手机号" />
      <!-- 滑块验证码组件 -->
      <SliderCaptcha v-model="captchaPassed" />
      <!-- 提交按钮 -->
      <button type="submit" :disabled="!captchaPassed">注册</button>
    </form>
  </div>
</template>

<script setup>
// 从Vue库导入响应式API
import { ref, reactive } from 'vue'
// 导入用户相关API函数
import { checkUsername, register } from '@/api/user'
// 导入自定义组件
import SmsInput from '@/components/SmsInput.vue'
import SliderCaptcha from '@/components/SliderCaptcha.vue'

// reactive用于创建响应式对象，当数据变化时UI会自动更新
// 表单数据对象
const form = reactive({ username:'', password:'', phone:'', smsCode:'' })
// 表单错误信息对象
const errors = reactive({ username:'', password:'' })
// ref用于创建独立的响应式值，captchaPassed跟踪滑块验证是否通过
const captchaPassed = ref(false)

// 异步函数，检查用户名是否可用
async function checkName() {
  // async/await是JavaScript处理异步操作的语法
  // ...调用 checkUsername，设置 errors.username...
}

// 发送短信验证码函数
function sendSms() {
  // ...通过后端接口发送手机验证码...
}

// 表单提交处理函数
async function onSubmit() {
  // ...最终校验 + register(...)
}
</script>

<style scoped>
.auth-container {
  max-width: 400px;
  margin: 50px auto;
  padding: 2rem;
  background: var(--card-bg);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.auth-container h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: var(--text-color);
}
.auth-container form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.auth-container input,
.auth-container button,
.auth-container .search-input,
.auth-container SmsInput {
  width: 100%;
}
.auth-container input {
  padding: 0.75rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--bg-color);
  color: var(--text-color);
}
.auth-container button {
  padding: 0.75rem;
  border: none;
  border-radius: 4px;
  background: var(--accent);
  color: #fff;
  font-weight: 500;
}
.auth-container button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
