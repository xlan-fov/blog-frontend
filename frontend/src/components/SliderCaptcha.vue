<template>
  <div class="slider-captcha">
    <!-- Canvas + 拖动逻辑，拖动成功后 $emit('update:modelValue', true) -->
    <!-- 这个组件内部会绘制一个Canvas滑块验证界面 -->
  </div>
</template>
<script setup>
// 定义组件属性
defineProps({ modelValue: Boolean })

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

// ...canvas 绘制 + 拖动校验逻辑...
// 验证成功时会调用 emit('update:modelValue', true)
</script>
