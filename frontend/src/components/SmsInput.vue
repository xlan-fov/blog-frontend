<template>
  <div>
    <!-- 手机号输入框 -->
    <input v-model="phone" placeholder="手机号" />
    
    <!-- 发送验证码按钮，:disabled属性根据countdown状态动态设置是否禁用 -->
    <button @click="sendCode" :disabled="countdown>0">
      {{ countdown>0? countdown+'s':'发送验证码' }}
    </button>
    
    <!-- 验证码输入框 -->
    <input v-model="code" placeholder="验证码" />
  </div>
</template>
<script setup>
// 导入Vue响应式API
import { ref, watch } from 'vue'

// 定义组件属性
defineProps({ modelValue: String })

// 定义组件事件
const emit = defineEmits(['update:modelValue','send'])

// 创建响应式的手机号、验证码和倒计时值
const phone = ref('')
const code  = ref('')
const countdown = ref(0)

// 发送验证码的函数
function sendCode() {
  // 触发send事件，将手机号作为参数传递
  emit('send', phone.value)
  // ...开始倒计时...
}

// 监听code变化，当变化时触发update:modelValue事件
// 这实现了自定义组件的双向绑定
watch(code, v => emit('update:modelValue', v))
</script>
