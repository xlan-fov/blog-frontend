<template>
  <div class="captcha-container" @click="refreshCaptcha">
    <img :src="captchaUrl" alt="验证码" class="captcha-image">
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 定义 modelValue prop，用于双向绑定验证码文本
const props = defineProps({
  modelValue: String
})

// 定义事件，用于更新验证码文本
const emit = defineEmits(['update:modelValue'])

// 验证码图片URL
const captchaUrl = ref('')

// 刷新验证码
const refreshCaptcha = () => {
  // 生成新的验证码URL，添加时间戳避免缓存
  captchaUrl.value = `/api/captcha?t=${Date.now()}`
  // 清空当前验证码文本
  emit('update:modelValue', '')
}

// 组件挂载时初始化验证码
onMounted(() => {
  refreshCaptcha()
})

// 对外暴露刷新验证码方法
defineExpose({
  refreshCaptcha
})
</script>

<style scoped>
.captcha-container {
  display: inline-block;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  height: 100%;
}

.captcha-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
</style>
