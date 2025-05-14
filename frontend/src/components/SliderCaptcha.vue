<template>
  <div class="slider-captcha">
    <div class="slider-track" ref="trackRef">
      <div class="slider-bg" :style="{ width: `${value}%` }"></div>
      <div class="slider-button" 
           ref="buttonRef"
           :class="{ success: value === 100 }"
           @mousedown="startDrag"
           @touchstart="startDrag">
        <div class="slider-icon">
          {{ value === 100 ? '✓' : '→' }}
        </div>
      </div>
      <div class="slider-text" :class="{ hidden: value > 0 }">
        请拖动滑块完成验证
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

// 定义 props，接收外部传入的值
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// 定义事件，用于向外部传递值的变化
const emit = defineEmits(['update:modelValue'])

// 滑块位置百分比
const value = ref(0)
// 滑动轨道和按钮的DOM引用
const trackRef = ref(null)
const buttonRef = ref(null)
// 记录拖动状态
const isDragging = ref(false)
// 记录起始位置
const startX = ref(0)
const startLeft = ref(0)

// 开始拖动
const startDrag = (e) => {
  if (value.value === 100) return
  
  isDragging.value = true
  
  // 兼容鼠标和触摸事件
  startX.value = e.type.startsWith('touch') 
    ? e.touches[0].clientX 
    : e.clientX
    
  startLeft.value = buttonRef.value.offsetLeft
  
  // 添加移动和结束事件监听
  document.addEventListener('mousemove', drag)
  document.addEventListener('touchmove', drag)
  document.addEventListener('mouseup', endDrag)
  document.addEventListener('touchend', endDrag)
}

// 拖动中
const drag = (e) => {
  if (!isDragging.value) return
  
  // 阻止默认行为，避免页面滚动等
  e.preventDefault()
  
  // 计算移动距离
  const clientX = e.type.startsWith('touch') 
    ? e.touches[0].clientX 
    : e.clientX
  const moveX = clientX - startX.value
  
  // 计算新位置
  const trackWidth = trackRef.value.offsetWidth
  const buttonWidth = buttonRef.value.offsetWidth
  const maxLeft = trackWidth - buttonWidth
  
  let newLeft = startLeft.value + moveX
  // 限制范围
  newLeft = Math.max(0, Math.min(newLeft, maxLeft))
  
  // 计算百分比
  const percent = Math.round((newLeft / maxLeft) * 100)
  value.value = percent
  
  // 如果完成验证
  if (percent === 100) {
    emit('update:modelValue', true)
    endDrag()
  }
}

// 结束拖动
const endDrag = () => {
  isDragging.value = false
  
  // 移除事件监听
  document.removeEventListener('mousemove', drag)
  document.removeEventListener('touchmove', drag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchend', endDrag)
  
  // 如果未完成验证，重置位置
  if (value.value < 100) {
    value.value = 0
  }
}

// 组件卸载时清理事件监听
onUnmounted(() => {
  document.removeEventListener('mousemove', drag)
  document.removeEventListener('touchmove', drag)
  document.removeEventListener('mouseup', endDrag)
  document.removeEventListener('touchend', endDrag)
})
</script>

<style scoped>
.slider-captcha {
  width: 100%;
  margin: 10px 0;
}

.slider-track {
  position: relative;
  height: 40px;
  background-color: #f5f5f5;
  border-radius: 20px;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.slider-bg {
  position: absolute;
  height: 100%;
  background-color: #4e6ef2;
  transition: width 0.1s;
  border-radius: 20px 0 0 20px;
}

.slider-button {
  position: absolute;
  top: 0;
  left: 0;
  width: 40px;
  height: 40px;
  background-color: white;
  border-radius: 50%;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.2s;
  z-index: 10;
}

.slider-button:hover {
  transform: scale(1.05);
}

.slider-button.success {
  background-color: #67c23a;
  color: white;
}

.slider-icon {
  font-size: 16px;
  color: #333;
}

.slider-button.success .slider-icon {
  color: white;
}

.slider-text {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
  user-select: none;
  transition: opacity 0.3s;
}

.slider-text.hidden {
  opacity: 0;
}
</style>
