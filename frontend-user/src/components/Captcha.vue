<template>
  <div class="captcha-container" @click="refreshCaptcha">
    <canvas ref="canvasRef" width="120" height="40"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const canvasRef = ref(null)
let captchaText = ''

const generateCaptcha = () => {
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  // 清空画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 生成4位随机数字
  captchaText = Math.random().toString().slice(2, 6)
  emit('update:modelValue', captchaText)
  
  // 设置背景
  ctx.fillStyle = '#f3f3f3'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  
  // 绘制文字
  ctx.fillStyle = '#333'
  ctx.font = 'bold 24px Arial'
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  
  // 随机旋转和位置
  for (let i = 0; i < captchaText.length; i++) {
    const x = 20 + i * 25
    const y = 20
    const rotate = (Math.random() - 0.5) * 0.3
    ctx.save()
    ctx.translate(x, y)
    ctx.rotate(rotate)
    ctx.fillText(captchaText[i], 0, 0)
    ctx.restore()
  }
  
  // 添加干扰线
  for (let i = 0; i < 4; i++) {
    ctx.beginPath()
    ctx.moveTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.lineTo(Math.random() * canvas.width, Math.random() * canvas.height)
    ctx.strokeStyle = `rgba(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255},0.5)`
    ctx.stroke()
  }
  
  // 添加干扰点
  for (let i = 0; i < 50; i++) {
    ctx.beginPath()
    ctx.arc(Math.random() * canvas.width, Math.random() * canvas.height, 1, 0, 2 * Math.PI)
    ctx.fillStyle = `rgba(${Math.random() * 255},${Math.random() * 255},${Math.random() * 255},0.5)`
    ctx.fill()
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

defineExpose({
  refreshCaptcha
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
</style> 