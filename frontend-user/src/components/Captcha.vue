<template>
  <div class="captcha-container">
    <img 
      :src="captchaUrl" 
      alt="验证码" 
      class="captcha-image"
      @click="refreshCaptcha"
      @error="handleImageError"
      title="点击更换验证码"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCaptcha } from '@/api/user'

const props = defineProps({
  onCaptchaChange: {
    type: Function,
    default: () => {}
  }
})

const captchaUrl = ref('')
const captchaId = ref('')

const generateCaptchaId = () => {
  return 'captcha_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const refreshCaptcha = async () => {
  try {
    const newCaptchaId = generateCaptchaId()
    captchaId.value = newCaptchaId
    
    const result = await getCaptcha(newCaptchaId)
    if (result.code === 200 || result.code === 0) {
      // 检查返回的数据格式，避免重复添加前缀
      let imageData = result.data.img
      
      // 如果已经是完整的data URL，直接使用
      if (imageData && imageData.startsWith('data:image/')) {
        captchaUrl.value = imageData
      } 
      // 如果只是base64字符串，添加前缀
      else if (imageData) {
        captchaUrl.value = 'data:image/png;base64,' + imageData
      } else {
        console.error('验证码图片数据为空')
        return
      }
      
      props.onCaptchaChange(newCaptchaId)
    } else {
      console.error('获取验证码失败:', result.msg)
    }
  } catch (error) {
    console.error('获取验证码失败:', error)
    // 不要在错误时无限重试
  }
}

const handleImageError = () => {
  console.error('验证码图片加载失败')
  refreshCaptcha()
}

onMounted(() => {
  refreshCaptcha()
})

defineExpose({
  refreshCaptcha
})
</script>

<style scoped>
.captcha-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-image {
  width: 120px;
  height: 40px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.captcha-image:hover {
  border-color: #409eff;
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

.captcha-image:active {
  transform: scale(0.98);
}
</style>