<template>
  <div>
    <!-- markdown 编辑区 + 预览区 -->
    <!-- 该组件会实现一个支持Markdown的富文本编辑器 -->
  </div>
</template>
<script setup>
// 导入Vue生命周期钩子
import { onMounted } from 'vue'

// 定义组件属性
defineProps({ modelValue: String })

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

// 组件挂载时的处理函数
onMounted(() => {
  // ...初始化编辑器，绑定 change 事件 emit 更新...
  // 当编辑器内容改变时，会触发update:modelValue事件更新父组件中的值
})
</script>
