import { defineStore } from 'pinia'
import { ref } from 'vue'
import userApi from '@/api/user'
import { ElMessage } from 'element-plus'

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
  const token = ref('')

  // 从localStorage恢复状态
  function restoreState() {
    const savedState = localStorage.getItem('userState')
    if (savedState) {
      const state = JSON.parse(savedState)
      userInfo.value = state.userInfo
      isLoggedIn.value = state.isLoggedIn
      token.value = localStorage.getItem('token') || ''
    }
  }

  // 保存状态到localStorage
  function saveState() {
    localStorage.setItem('userState', JSON.stringify({
      userInfo: userInfo.value,
      isLoggedIn: isLoggedIn.value
    }))

    if (token.value) {
      localStorage.setItem('token', token.value)
    }
  }

  // 普通用户登录
  async function login(username, password) {
    console.log('开始登录流程', { username })
    // 如果尝试使用超管账号在普通入口登录，直接返回失败
    const isAdminAccount = ADMIN_ACCOUNTS.some(admin => admin.username === username)
    if (isAdminAccount) {
      userInfo.value.loginAttempts++
      return false
    }

    try {
      const res = await userApi.loginByUsername({ username, password })
      console.log('登录响应详情:', res) // 添加详细日志
      
      if (res.code === 200 && res.data) {
        userInfo.value.username = res.data.username || username
        userInfo.value.avatar = res.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
        userInfo.value.bio = res.data.bio || '这是一个普通用户'
        userInfo.value.registerTime = res.data.registerTime || new Date().toISOString()
        userInfo.value.role = res.data.role || 'user'
        userInfo.value.status = res.data.status || 'normal'
        
        // 确保正确保存token
        if (res.data.token) {
          token.value = res.data.token
          // 保存到localStorage
          localStorage.setItem('token', res.data.token)
          console.log('成功保存token:', res.data.token)
        } else {
          console.warn('响应中没有token')
        }
        
        isLoggedIn.value = true
        userInfo.value.loginAttempts = 0
        saveState() // 保存状态
        
        // 打印当前认证状态
        console.log('登录成功，用户状态:', {
          isLoggedIn: isLoggedIn.value,
          username: userInfo.value.username,
          role: userInfo.value.role,
          token: token.value.substring(0, 10) + '...'  // 截断，避免打印完整token
        })
        
        // 如果用户被封禁，返回封禁状态
        if (userInfo.value.status === 'banned') {
          return 'banned'
        }
        
        return true
      } else {
        ElMessage.error(res.message || '登录失败')
        userInfo.value.loginAttempts++
        return false
      }
    } catch (error) {
      console.error('登录失败:', error)
      userInfo.value.loginAttempts++
      return false
    }
  }

  // 超管登录（需提供授权码）
  async function adminLogin(username, password, authCode) {
    try {
      // 在实际应用中，这里应该是一个API调用
      // 暂时使用本地验证
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
        token.value = 'admin-token' // 实际应该从API返回
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
    } catch (error) {
      console.error('管理员登录失败:', error)
      logFailedAdminLogin(username)
      return false
    }
  }

  // 记录管理员登录日志
  async function logAdminLogin() {
    try {
      // 实际API请求，记录管理员登录操作
      // 可能需要调用管理员日志API
      console.log('管理员登录:', userInfo.value.username, new Date().toISOString())
    } catch (error) {
      console.error('记录管理员登录日志失败:', error)
    }
  }

  // 记录失败的管理员登录尝试
  async function logFailedAdminLogin(username) {
    try {
      // 实际API请求，记录失败的管理员登录尝试
      console.log('失败的管理员登录尝试:', username, new Date().toISOString())
    } catch (error) {
      console.error('记录失败的管理员登录尝试失败:', error)
    }
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
    token.value = ''
    localStorage.removeItem('userState') // 清除状态
    localStorage.removeItem('token') // 清除token
  }

  // 记录管理员登出日志
  async function logAdminLogout() {
    try {
      // 实际API请求，记录管理员登出操作
      console.log('管理员登出:', userInfo.value.username, new Date().toISOString())
    } catch (error) {
      console.error('记录管理员登出日志失败:', error)
    }
  }

  // 获取用户个人资料
  async function fetchProfile() {
    try {
      const res = await userApi.getProfile()
      if (res.code === 200 && res.data) {
        userInfo.value = {
          ...userInfo.value,
          ...res.data
        }
        saveState()
        return true
      } else {
        ElMessage.error(res.message || '获取个人资料失败')
        return false
      }
    } catch (error) {
      console.error('获取个人资料失败:', error)
      return false
    }
  }

  // 更新用户个人资料
  async function updateProfile(avatar, bio) {
    try {
      const res = await userApi.updateProfile({ avatar, bio })
      if (res.code === 200) {
        userInfo.value.avatar = avatar
        userInfo.value.bio = bio
        saveState()
        return true
      } else {
        ElMessage.error(res.message || '更新个人资料失败')
        return false
      }
    } catch (error) {
      console.error('更新个人资料失败:', error)
      return false
    }
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
  async function banUser(username, reason) {
    try {
      // 要求管理员权限
      if (!isAdmin()) {
        ElMessage.error('权限不足')
        return false
      }
      
      const res = await adminApi.banUser(username, { reason })
      if (res.code === 200) {
        // 如果是当前用户，更新用户状态
        if (userInfo.value.username === username) {
          userInfo.value.status = 'banned'
          saveState()
        }
        return true
      } else {
        ElMessage.error(res.message || '封禁用户失败')
        return false
      }
    } catch (error) {
      console.error('封禁用户失败:', error)
      return false
    }
  }

  // 解封账号
  async function unbanUser(username) {
    try {
      // 要求管理员权限
      if (!isAdmin()) {
        ElMessage.error('权限不足')
        return false
      }
      
      const res = await adminApi.unbanUser(username, {})
      if (res.code === 200) {
        // 如果是当前用户，更新用户状态
        if (userInfo.value.username === username) {
          userInfo.value.status = 'normal'
          saveState()
        }
        return true
      } else {
        ElMessage.error(res.message || '解封用户失败')
        return false
      }
    } catch (error) {
      console.error('解封用户失败:', error)
      return false
    }
  }

  // 初始化时恢复状态
  restoreState()

  return {
    userInfo,
    isLoggedIn,
    login,
    adminLogin,
    logout,
    fetchProfile,
    updateProfile,
    isAdmin,
    isBanned,
    banUser,
    unbanUser
  }
})