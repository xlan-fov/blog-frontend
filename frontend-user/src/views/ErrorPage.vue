<template>
  <div class="error-page">
    <el-result
      :icon="errorIcon"
      :title="errorTitle"
      :sub-title="errorSubTitle"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const errorType = computed(() => route.params.type || '404')

const errorConfig = {
  '404': {
    icon: 'error',
    title: '404',
    subTitle: '抱歉，您访问的页面不存在'
  },
  '403': {
    icon: 'warning',
    title: '403',
    subTitle: '抱歉，您没有权限访问此页面'
  },
  '500': {
    icon: 'error',
    title: '500',
    subTitle: '抱歉，服务器出错了'
  }
}

const errorIcon = computed(() => errorConfig[errorType.value]?.icon || 'error')
const errorTitle = computed(() => errorConfig[errorType.value]?.title || '错误')
const errorSubTitle = computed(() => errorConfig[errorType.value]?.subTitle || '发生了一些错误')

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
</style> 