<template>
  <div class="pagination">
    <!-- 上一页按钮 -->
    <!-- @click绑定点击事件，触发change事件并传递currentPage-1作为参数 -->
    <!-- :disabled是动态绑定disabled属性，当currentPage<=1时禁用按钮 -->
    <button @click="
$emit('change', currentPage - 1)" :disabled="currentPage <= 1">
      上一页
    </button>
    
    <!-- 页码信息显示 -->
    <span>第 {{ currentPage }} 页 / 共 {{ totalPages }} 页</span>
    
    <!-- 下一页按钮 -->
    <button
      @click="$emit('change', currentPage + 1)"
      :disabled="currentPage >= totalPages"
    >
      下一页
    </button>
  </div>
</template>
<script setup>
// 导入Vue计算属性API
import { computed } from 'vue'

// 定义组件属性
const props = defineProps({
  // 当前页码
  currentPage: Number,
  // 总记录数
  total: Number,
})

// 计算总页数，每页显示10条记录
// computed创建计算属性，它会根据依赖的响应式数据自动更新
const totalPages = computed(() => Math.ceil(props.total / 10))
</script>