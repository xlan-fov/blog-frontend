<template>
  <div class="tag-selector">
    <!-- 遍历标签数组，为每个标签创建一个展示项 -->
    <!-- v-for用于列表渲染，:key是列表项的唯一标识，优化渲染性能 -->
    <span v-for="tag in tags" :key="tag" class="tag-item">
      {{ tag }}
      <!-- 删除按钮，点击时调用remove函数 -->
      <button type="button" @click="remove(tag)">×</button>
    </span>
    
    <!-- 输入框，用于添加新标签 -->
    <input
      v-model="input"
      @keyup.enter="add"
      placeholder="输入标签后回车"
    />
  </div>
</template>
<script setup>
// 导入Vue响应式API
import { ref, watch } from 'vue'

// 定义组件属性
const props = defineProps({
  // modelValue是v-model绑定的值
  modelValue: { type: Array, default: () => [] }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue'])

// 创建响应式的标签数组，使用扩展运算符复制props中的值
const tags = ref([...props.modelValue])
// 创建响应式的输入框值
const input = ref('')

// 添加标签的函数
function add() {
  // 去除输入值两端空白
  const val = input.value.trim()
  // 如果输入值不为空且不在标签数组中，则添加
  if (val && !tags.value.includes(val)) {
    tags.value.push(val)
  }
  // 清空输入框
  input.value = ''
}

// 移除标签的函数
function remove(t) {
  // 过滤掉要删除的标签
  tags.value = tags.value.filter(x => x !== t)
}

// 监听tags变化，当变化时触发update:modelValue事件
// 这实现了自定义组件的双向绑定
watch(tags, v => emit('update:modelValue', v), { deep: true })

// 监听props.modelValue变化，同步到本地tags变量
watch(() => props.modelValue, v => { tags.value = [...v] })
</script>

<style scoped>
/* 标签选择器容器样式 */
.tag-selector { display: flex; flex-wrap: wrap; gap: 4px; align-items: center; }
/* 标签项样式 */
.tag-item {
  background: #eef; padding: 2px 6px; border-radius: 4px;
}
/* 标签删除按钮样式 */
.tag-item button { border: none; background: transparent; cursor: pointer; margin-left: 4px; }
/* 输入框样式 */
.tag-selector input { flex: 1; min-width: 80px; padding: 4px; }
</style>
