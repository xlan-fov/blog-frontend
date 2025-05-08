<template>
  <div class="search-input">
    <!-- 搜索输入框 -->
    <!-- :value绑定了输入框的值，而不是用v-model，这是为了手动控制数据流向 -->
    <input
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      @keyup.enter="$emit('search')"
      :placeholder="placeholder"
    />
    
    <!-- 搜索按钮 -->
    <button @click="$emit('search')">搜索</button>
  </div>
</template>

<script setup>
// 定义组件属性
const props = defineProps({
  // modelValue是v-model绑定的值
  modelValue: { type: String, default: '' },
  // placeholder是输入框的提示文本
  placeholder: { type: String, default: '' }
})

// 定义组件事件
const emit = defineEmits(['update:modelValue', 'search'])
</script>

<style scoped>
/* 搜索输入框容器样式，美化参考百度 */
.search-input {
  display: flex;
  align-items: center;
  margin: 0 auto 16px;
  width: 100%;
  max-width: 600px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  border-radius: 4px;
  overflow: hidden;
}
.search-input input {
  flex: 1;
  height: 40px;
  padding: 0 12px;
  border: none;
  font-size: 16px;
  outline: none;
}
.search-input button {
  height: 40px;
  padding: 0 20px;
  background: #3385ff;
  color: #fff;
  border: none;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s;
}
.search-input button:hover {
  background: #2672ff;
}
</style>
