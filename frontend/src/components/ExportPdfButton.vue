<template>
  <!-- 导出PDF按钮，点击时调用downloadPdf函数 -->
  <button @click="downloadPdf">导出 PDF</button>
</template>

<script setup>
// 导入文章API
import { exportPdf } from '@/api/article'

// 定义组件属性
const props = defineProps({ id: Number })

// 下载PDF的函数
async function downloadPdf() {
  // 调用API获取PDF数据
  const res = await exportPdf(props.id);
  
  // 创建Blob对象，指定类型为PDF
  const blob = new Blob([res.data], { type: 'application/pdf' });
  
  // 创建临时URL
  const url  = window.URL.createObjectURL(blob);
  
  // 创建临时a标签用于下载
  const a    = document.createElement('a');
  a.href       = url;
  a.download   = `article_${props.id}.pdf`;
  
  // 将a标签添加到DOM并模拟点击，然后移除
  document.body.appendChild(a);
  a.click();
  a.remove();
}
</script>

<style scoped>
/* 根据需要添加按钮或模态框样式 */
</style>
