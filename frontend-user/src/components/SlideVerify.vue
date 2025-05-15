<!-- components/VerifySlider.vue -->
<template>
  <div ref="verifyContainer" class="slide-verify-container">
    <slide-verify
      v-if="reload"
      ref="verify"
      :l="42"
      :r="8"
      :w="width"
      :h="160"
      :slider-text="sliderText"
      :accuracy="accuracy"
      :show-refresh="showRefresh"
      :imgs="bgList"
      :default-img="currentImg"
      @again="verifyAgain"
      @success="onSuccess"
      @fail="onFail"
      @refresh="onRefresh"
    />
    <div v-if="verifyMsg" :class="['verify-message', isVerifySuccess ? 'success' : 'error']">
      {{ verifyMsg }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, nextTick, onBeforeUnmount, computed } from 'vue';
import SlideVerify from 'vue3-slide-verify';
import 'vue3-slide-verify/dist/style.css';
import img from '@/assets/logo.jpg'; // ✅ 按需更换

const props = defineProps({
  width: {
    type: Number,
    default: 320
  },
  sliderText: {
    type: String,
    default: '向右滑动完成验证'
  },
  accuracy: {
    type: Number,
    default: 5
  },
  showRefresh: {
    type: Boolean,
    default: true
  }
});

const verify = ref(null);
const verifyContainer = ref(null);
const reload = ref(false);
const isVerifySuccess = ref(false);
const verifyMsg = ref('');
const width = ref(props.width);

const bgList = [
  // 'https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/slider-1.jpg',
  'https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/slider-2.jpg',
  // 'https://sentiblog-repo.oss-cn-wuhan-lr.aliyuncs.com/assets/slider-3.jpg'
];

const currentImg = computed(() => {
  return bgList[Math.floor(Math.random() * bgList.length)];
});

// 监听窗口大小变化
const handleResize = () => {
  reload.value = false;
  nextTick(() => {
    setVerifyWidth();
  });
};

onMounted(() => {
  setVerifyWidth();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});

async function setVerifyWidth() {
  await nextTick();
  if (verifyContainer.value) {
    const containerWidth = verifyContainer.value.offsetWidth;
    width.value = Math.min(props.width, containerWidth - 40); // 留出左右边距
    reload.value = true;
  }
}

function onSuccess(times) {
  isVerifySuccess.value = true;
  verifyMsg.value = `验证通过 (${(times / 1000).toFixed(1)}s)`;
  emit('success');
}

function onFail() {
  isVerifySuccess.value = false;
  verifyMsg.value = '验证失败，请重试';
}

function onRefresh() {
  isVerifySuccess.value = false;
  verifyMsg.value = '';
  reload.value = false;
  nextTick(() => {
    reload.value = true;
  });
}

function verifyAgain() {
  isVerifySuccess.value = false;
  verifyMsg.value = '';
}

// 提供重置方法给父组件
function reset() {
  reload.value = false;
  nextTick(() => {
    reload.value = true;
  });
  isVerifySuccess.value = false;
  verifyMsg.value = '';
}


const emit = defineEmits(['success']);

defineExpose({
  reset
});
</script>

<style scoped>
.slide-verify-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
  box-sizing: border-box;
}

.verify-message {
  margin-top: 12px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.verify-message.success {
  color: #67c23a;
}

.verify-message.error {
  color: #f56c6c;
}

:deep(.slide-verify-slider) {
  height: 40px !important;
  line-height: 40px !important;
  background-color: #f5f7fa !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

:deep(.slide-verify-slider-mask) {
  background-color: #409eff !important;
  opacity: 0.8 !important;
}

:deep(.slide-verify-slider-icon) {
  font-size: 20px !important;
}

:deep(.slide-verify) {
  border: 1px solid #dcdfe6 !important;
  border-radius: 4px !important;
  overflow: hidden !important;
}

:deep(.slide-verify-refresh-icon) {
  font-size: 16px !important;
  color: #909399 !important;
}
</style>
