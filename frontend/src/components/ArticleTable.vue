<template>
  <!-- 文章表格 -->
  <table class="article-table">
    <!-- 表头 -->
    <thead>
      <tr>
        <th>标题</th>
        <th>状态</th>
        <th>更新时间</th>
        <th>操作</th>
      </tr>
    </thead>
    <!-- 表体 -->
    <tbody>
      <!-- 使用v-for循环渲染每行数据 -->
      <tr v-for="item in articles" :key="item.id">
        <!-- 标题列，包含链接到文章详情页 -->
        <td>
          <router-link :to="`/articles/${item.id}`">{{ item.title }}</router-link>
        </td>
        <!-- 状态列 -->
        <td>{{ item.status }}</td>
        <!-- 更新时间列，使用Date对象格式化时间 -->
        <td>{{ new Date(item.updated_at).toLocaleString() }}</td>
        <!-- 操作列，包含编辑和删除按钮 -->
        <td>
          <EditArticleModal :article="item" @saved="$emit('delete')" />
          <DeleteConfirmDialog :id="item.id" @deleted="$emit('delete')" />
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script setup>
  // 导入自定义组件
  import EditArticleModal from './EditArticleModal.vue'
  import DeleteConfirmDialog from './DeleteConfirmDialog.vue'

  // 定义组件属性
  defineProps({ articles: Array })
</script>
<style scoped>
/* 垂直居中表格内容 */
th, td {
  vertical-align: middle;
}
</style>