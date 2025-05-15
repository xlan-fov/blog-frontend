import { defineStore } from 'pinia'
import { ref } from 'vue'

// 模拟的超管账号列表（实际应用中应该从后端获取）
const ADMIN_ACCOUNTS = [
  {
    username: 'superadmin',
    password: '123456',
    authCode: 'sentiblog2023', // 管理员授权码
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    bio: '系统超级管理员',
    registerTime: '2023-01-01T00:00:00.000Z'
  },
  {
    username: 'admin',
    password: '123456',
    authCode: 'sentiblog2023', // 管理员授权码
    avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
    bio: '管理员',
    registerTime: '2023-01-01T00:00:00.000Z'
  }
]

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    username: '',
    avatar: '',
    bio: '',
    registerTime: '',
    loginAttempts: 0,
    role: '', // 角色字段：'admin'为超管，'user'为普通用户
    status: 'normal' // 状态字段：'normal'为正常，'banned'为封禁
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

  // 普通用户登录
  function login(username, password) {
    // 如果尝试使用超管账号在普通入口登录，直接返回失败
    const isAdminAccount = ADMIN_ACCOUNTS.some(admin => admin.username === username)
    if (isAdminAccount) {
      userInfo.value.loginAttempts++
      return false
    }

    // TODO: 替换为实际的API请求
    // 模拟普通用户登录逻辑
    if (username === 'user' && password === '123456') {
      userInfo.value.username = username
      userInfo.value.avatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
      userInfo.value.bio = '这是一个普通用户'
      userInfo.value.registerTime = new Date().toISOString()
      userInfo.value.role = 'user' // 设置为普通用户角色
      userInfo.value.status = 'normal'
      isLoggedIn.value = true
      userInfo.value.loginAttempts = 0
      saveState() // 保存状态
      return true
    } else if (username === 'banned' && password === '123456') {
      // 模拟被封禁的账号
      userInfo.value.username = username
      userInfo.value.status = 'banned'
      userInfo.value.loginAttempts = 0
      return 'banned'
    }
    userInfo.value.loginAttempts++
    return false
  }

  // 超管登录（需提供授权码）
  function adminLogin(username, password, authCode) {
    // 查找管理员账号
    const adminAccount = ADMIN_ACCOUNTS.find(
      admin => admin.username === username &&
        admin.password === password &&
        admin.authCode === authCode
    )

    if (adminAccount) {
      userInfo.value.username = adminAccount.username
      userInfo.value.avatar = adminAccount.avatar
      userInfo.value.bio = adminAccount.bio
      userInfo.value.registerTime = adminAccount.registerTime
      userInfo.value.role = 'admin' // 设置为超级管理员角色
      userInfo.value.status = 'normal'
      isLoggedIn.value = true
      userInfo.value.loginAttempts = 0
      saveState() // 保存状态

      // 记录管理员登录日志
      logAdminLogin()

      return true
    }

    // 记录失败的管理员登录尝试
    logFailedAdminLogin(username)
    return false
  }

  // 记录管理员登录日志
  function logAdminLogin() {
    // TODO: 替换为实际API请求，记录管理员登录操作
    console.log('管理员登录:', userInfo.value.username, new Date().toISOString())
  }

  // 记录失败的管理员登录尝试
  function logFailedAdminLogin(username) {
    // TODO: 替换为实际API请求，记录失败的管理员登录尝试
    console.log('失败的管理员登录尝试:', username, new Date().toISOString())
  }

  function logout() {
    // 记录管理员登出日志
    if (userInfo.value.role === 'admin') {
      logAdminLogout()
    }

    userInfo.value = {
      username: '',
      avatar: '',
      bio: '',
      registerTime: '',
      loginAttempts: 0,
      role: '',
      status: 'normal'
    }
    isLoggedIn.value = false
    localStorage.removeItem('userState') // 清除状态
  }

  // 记录管理员登出日志
  function logAdminLogout() {
    // TODO: 替换为实际API请求，记录管理员登出操作
    console.log('管理员登出:', userInfo.value.username, new Date().toISOString())
  }

  function updateProfile(avatar, bio) {
    userInfo.value.avatar = avatar
    userInfo.value.bio = bio
    saveState() // 保存状态
  }

  // 判断是否为超级管理员
  function isAdmin() {
    return userInfo.value.role === 'admin'
  }

  // 判断账号是否被封禁
  function isBanned() {
    return userInfo.value.status === 'banned'
  }

  // 封禁账号
  function banUser(username, reason) {
    // TODO: 替换为实际的API请求
    // 模拟封禁逻辑
    if (userInfo.value.username === username) {
      userInfo.value.status = 'banned'
      saveState()
      return true
    }
    return false
  }

  // 解封账号
  function unbanUser(username) {
    // TODO: 替换为实际的API请求
    // 模拟解封逻辑
    if (userInfo.value.username === username) {
      userInfo.value.status = 'normal'
      saveState()
      return true
    }
    return false
  }

  // 初始化时恢复状态
  restoreState()

  return {
    userInfo,
    isLoggedIn,
    login,
    adminLogin,
    logout,
    updateProfile,
    isAdmin,
    isBanned,
    banUser,
    unbanUser
  }
}) 