<template>
  <div class="captcha-container" @click="refreshCaptcha">
    <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" width="120" height="40" />
    <div v-else class="captcha-placeholder">点击获取验证码</div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getCaptcha } from '@/api/user'

const props = defineProps({
  username: {
    type: String,
    required: true
  },
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'code-generated'])

const captchaUrl = ref('')
const captchaCode = ref('')

watch(() => props.username, (newVal) => {
  if (newVal && newVal.trim() !== '') {
    generateCaptcha()
  } else {
    captchaUrl.value = ''
  }
}, { immediate: true })

const generateCaptcha = async () => {
  if (!props.username || props.username.trim() === '') {
    return
  }
  
  try {
    const response = await getCaptcha(props.username)
    
    if (response.code === 200 && response.data) {
      captchaUrl.value = response.data.img || response.data
      captchaCode.value = response.data.code || ''
      emit('code-generated', captchaCode.value)
    } else {
      console.error('获取验证码失败:', response.message || '未知错误')
      captchaUrl.value = ''
    }
  } catch (error) {
    console.error('获取验证码出错:', error)
    captchaUrl.value = ''
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
}

onMounted(() => {
  if (props.username && props.username.trim() !== '') {
    generateCaptcha()
  }
})
</script>

<style scoped>
.captcha-container {
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
  border: 1px solid #dcdfe6;
  width: 120px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-container:hover {
  opacity: 0.8;
}

.captcha-container img {
  display: block;
  width: 120px;
  height: 40px;
  object-fit: cover;
}

.captcha-placeholder {
  font-size: 12px;
  color: #909399;
  text-align: center;
}
</style>
