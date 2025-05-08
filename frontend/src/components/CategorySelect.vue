<template>
  <!-- 分类下拉选择框 -->
  <select v-model="selected">
    <!-- 使用v-for循环渲染选项 -->
    <option v-for="o in options" :key="o.id" :value="o.id">{{ o.name }}</option>
  </select>
</template>
<script setup>
// 导入Vue响应式API和生命周期钩子
import { ref, onMounted, watch } from 'vue'

// 定义组件属性
const props = defineProps({
  // modelValue是v-model绑定的值
  modelValue: { type: [Number, String], default: null }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

// 分类选项数组
const options = ref([])
// 当前选中的值
const selected = ref(props.modelValue)

// 组件挂载时的处理函数
onMounted(() => {
  // ...拉取分类列表接口赋值给 options...
})

// 监听selected变化，当变化时触发update:modelValue事件
watch(selected, v => emit('update:modelValue', v))

// 监听props.modelValue变化，同步到本地selected变量
watch(() => props.modelValue, v => { selected.value = v })
</script>
