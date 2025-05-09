import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    username: '',
    avatar: '',
    bio: '',
    registerTime: '',
    loginAttempts: 0
  })

  const isLoggedIn = ref(false)

  // 从localStorage恢复状态
  function restoreState() {
    const savedState = localStorage.getItem('userState')
    if (savedState) {
      const state = JSON.parse(savedState)
      userInfo.value = state.userInfo
      isLoggedIn.value = state.isLoggedIn
    }
  }

  // 保存状态到localStorage
  function saveState() {
    localStorage.setItem('userState', JSON.stringify({
      userInfo: userInfo.value,
      isLoggedIn: isLoggedIn.value
    }))
  }

  function login(username, password) {
    // 模拟登录逻辑
    if (username === 'admin' && password === '123456') {
      userInfo.value.username = username
      userInfo.value.avatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      userInfo.value.bio = '这是一个测试用户'
      userInfo.value.registerTime = new Date().toISOString()
      isLoggedIn.value = true
      userInfo.value.loginAttempts = 0
      saveState() // 保存状态
      return true
    }
    userInfo.value.loginAttempts++
    return false
  }

  function logout() {
    userInfo.value = {
      username: '',
      avatar: '',
      bio: '',
      registerTime: '',
      loginAttempts: 0
    }
    isLoggedIn.value = false
    localStorage.removeItem('userState') // 清除状态
  }

  function updateProfile(avatar, bio) {
    userInfo.value.avatar = avatar
    userInfo.value.bio = bio
    saveState() // 保存状态
  }

  // 初始化时恢复状态
  restoreState()

  return {
    userInfo,
    isLoggedIn,
    login,
    logout,
    updateProfile
  }
}) 