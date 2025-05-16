<template>
  <div class="captcha-container" @click="refreshCaptcha">
    <img v-if="captchaUrl" :src="captchaUrl" alt="验证码" width="120" height="40" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import { getCaptcha } from '@/api/captcha'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  onCaptchaChange: {
    type: Function,
    default: () => {}
  }
})

const emit = defineEmits(['update:modelValue'])

const captchaUrl = ref('')
const captchaId = ref('')

const generateCaptcha = async () => {
  try {
    // 生成唯一标识符
    const timestamp = Date.now()
    const uuid = uuidv4()
    const newCaptchaId = `${timestamp}-${uuid}`
    captchaId.value = newCaptchaId

    const response = await getCaptcha(newCaptchaId)
    
    if (response.code === 0) {
      captchaUrl.value = response.data.img
      // 通知父组件captchaId发生变化
      props.onCaptchaChange(newCaptchaId)
    } else {
      console.error('获取验证码失败:', response.msg)
    }
  } catch (error) {
    console.error('获取验证码出错:', error)
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({
  refreshCaptcha,
  getCaptchaId: () => captchaId.value
})
</script>

<style scoped>
.captcha-container {
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
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
</style> 