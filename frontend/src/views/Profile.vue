<template>
  <div class="profile">
    <!-- 头像显示和上传 -->
    <!-- :src是动态绑定属性，显示user对象中的avatar属性值 -->
    <img :src="user.avatar" alt="头像" />
    <!-- 文件上传输入框，change事件触发uploadAvatar函数 -->
    <input type="file" @change="uploadAvatar" />
    
    <!-- 注册时间显示 -->
    <div>注册时间：{{ user.registeredAt }}</div>
    
    <!-- 用户简介编辑框，v-model实现双向数据绑定 -->
    <textarea v-model="user.bio" placeholder="简介"></textarea>
    
    <!-- 保存按钮，点击时调用saveProfile函数 -->
    <button @click="saveProfile">保存</button>
    <!-- 修改密码按钮，点击时调用openChangePasswordDialog函数 -->
    <button @click="openChangePasswordDialog">修改密码</button>
  </div>
</template>
<script setup>
// 导入Vue的响应式API和生命周期钩子
import { ref, onMounted } from 'vue'
// 导入用户相关API函数
import { getProfile, updateProfile, changePassword } from '@/api/user'

// 创建响应式的用户数据对象
const user = ref({})

// 组件挂载时自动加载用户资料
onMounted(async () => {
  // 调用API获取用户资料
  const res = await getProfile(); 
  // 更新user的值，使用.value是因为ref对象需要通过.value访问实际值
  user.value = res.data
})

// 保存个人资料的函数
async function saveProfile() {
  // 调用API更新用户简介
  await updateProfile({ bio: user.value.bio })
}

// 上传头像的函数
function uploadAvatar(e) {
  // ...上传图片接口...
  // e是事件对象，可以通过e.target.files获取选择的文件
}

// 打开修改密码对话框的函数
function openChangePasswordDialog() {
  // ...弹出修改密码对话框...
}
</script>
