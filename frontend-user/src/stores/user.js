import { defineStore } from 'pinia'
import { ref } from 'vue'
import userApi from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    id: '', // 添加用户ID字段
    username: '',
    avatar: '',
    bio: '',
    phone: '', // 添加手机号字段
    registerTime: '',
    lastLoginTime: '', // 添加最后登录时间字段
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
  async function login(username, password, sliderData = null) {
    console.log('开始登录流程', { username })

    try {
      const loginParams = { username, password }
      if (sliderData) {
        loginParams.sliderToken = sliderData.token
        loginParams.sliderX = sliderData.x
      }

      const res = await userApi.loginByUsername(loginParams)
      console.log('登录响应详情:', res)

      if (res.code === 200 && res.data) {
        // 保存token
        if (res.data.token) {
          const receivedToken = res.data.token;
          token.value = receivedToken;
          localStorage.setItem('token', receivedToken);
        } else {
          console.warn('响应中没有token字段');
          return false;
        }

        // 登录成功后获取完整的用户信息
        try {
          console.log('开始获取用户信息...')
          const profileRes = await userApi.getProfile()
          console.log('获取到的用户信息:', profileRes)
          if (profileRes.code === 200 && profileRes.data) {
            console.log('解析用户数据:', profileRes.data)
            userInfo.value = {
              id: profileRes.data.id || res.data.userId,
              username: profileRes.data.username || username,
              avatar: profileRes.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
              bio: profileRes.data.description || '这是一个普通用户',
              phone: profileRes.data.phone || '',
              registerTime: profileRes.data.registerTime,
              lastLoginTime: profileRes.data.lastLoginTime,
              loginAttempts: 0,
              role: profileRes.data.role || 'user',
              status: profileRes.data.status || 'normal'
            }
            console.log('更新后的用户信息:', userInfo.value)
          } else {
            console.warn('获取用户信息失败:', profileRes)
            // 如果获取用户信息失败，使用登录响应中的基本信息
            userInfo.value = {
              id: res.data.userId,
              username: res.data.username || username,
              avatar: res.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
              bio: res.data.description || '这是一个普通用户',
              phone: res.data.phone || '',
              registerTime: res.data.registerTime || new Date().toISOString(),
              lastLoginTime: res.data.lastLoginTime || new Date().toISOString(),
              loginAttempts: 0,
              role: res.data.role || 'user',
              status: res.data.status || 'normal'
            }
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，使用登录响应中的基本信息
          userInfo.value = {
            id: res.data.userId,
            username: res.data.username || username,
            avatar: res.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
            bio: res.data.description || '这是一个普通用户',
            phone: res.data.phone || '',
            registerTime: res.data.registerTime || new Date().toISOString(),
            lastLoginTime: res.data.lastLoginTime || new Date().toISOString(),
            loginAttempts: 0,
            role: res.data.role || 'user',
            status: res.data.status || 'normal'
          }
        }

        isLoggedIn.value = true
        saveState() // 保存状态

        console.log('登录成功，用户ID:', userInfo.value.id)

        // 如果用户被封禁，返回封禁状态
        if (userInfo.value.status === 'banned') {
          return 'banned'
        }

        return true
      } else if (res.code === 401 && res.data?.needSlider) {
        // 需要滑动验证
        return { needSlider: true, sliderData: res.data }
      } else {
        ElMessage.error(res.message || '登录失败')
        return false
      }
    } catch (error) {
      console.error('登录失败:', error)
      return false
    }
  }

  // 管理员登录 - 使用专门的管理员登录API
  async function adminLogin(username, password, authCode) {
    try {
      // 使用专门的管理员登录API
      const res = await userApi.adminLogin({
        username,
        password,
        authCode
      });

      if (res.code === 200 && res.data) {
        // 保存token
        if (res.data.token) {
          token.value = res.data.token;
          localStorage.setItem('token', res.data.token);
        } else {
          // 如果没有token但登录成功，生成固定测试token用于开发
          const fixedTestToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6OTk5LCJ1c2VyTmFtZSI6ImFkbWluIiwiZXhwIjoyNTM0MDIzMDA4MDAsImlhdCI6MTY4NDc3NjAwMH0.H3rmvTpIKTjDCGPjgqJLKNmwfVHqYvlNL1uR17JxMHs';
          token.value = fixedTestToken;
          localStorage.setItem('token', fixedTestToken);
          console.log('使用固定测试token用于开发');
        }

        // 登录成功后获取完整的用户信息
        try {
          const profileRes = await userApi.getProfile()
          if (profileRes.code === 200 && profileRes.data) {
            console.log('获取到的用户信息:', profileRes)
            userInfo.value = {
              id: profileRes.data.id || res.data.userId,
              username: profileRes.data.username || username,
              avatar: profileRes.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
              bio: profileRes.data.description || '',
              phone: profileRes.data.phone || '',
              registerTime: profileRes.data.registerTime,
              lastLoginTime: profileRes.data.lastLoginTime,
              loginAttempts: 0,
              role: 'admin',
              status: profileRes.data.status || 'normal'
            }
          } else {
            // 如果获取用户信息失败，使用登录响应中的基本信息
            userInfo.value = {
              id: res.data.userId,
              username: res.data.username || username,
              avatar: res.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
              bio: res.data.description || '管理员',
              phone: res.data.phone || '',
              registerTime: res.data.registerTime || new Date().toISOString(),
              lastLoginTime: res.data.lastLoginTime || new Date().toISOString(),
              loginAttempts: 0,
              role: 'admin',
              status: res.data.status || 'normal'
            }
          }
        } catch (error) {
          console.error('获取用户信息失败:', error)
          // 如果获取用户信息失败，使用登录响应中的基本信息
          userInfo.value = {
            id: res.data.userId,
            username: res.data.username || username,
            avatar: res.data.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
            bio: res.data.description || '管理员',
            phone: res.data.phone || '',
            registerTime: res.data.registerTime || new Date().toISOString(),
            lastLoginTime: res.data.lastLoginTime || new Date().toISOString(),
            loginAttempts: 0,
            role: 'admin',
            status: res.data.status || 'normal'
          }
        }

        isLoggedIn.value = true
        saveState() // 保存状态

        console.log('管理员登录成功，用户ID:', userInfo.value.id)
        return true
      } else {
        ElMessage.error(res.message || '管理员登录失败')
        return false
      }
    } catch (error) {
      console.error('管理员登录失败:', error)
      return false
    }
  }

  // 设置用户信息
  function setUserInfo(userData) {
    // 只有在有新token时才清理旧token
    if (userData.token) {
      // 清除旧token，确保不会复用
      const oldToken = token.value
      if (oldToken && oldToken !== userData.token) {
        console.log('替换旧token:', oldToken.substring(0, 20) + '...')
      }

      token.value = userData.token
      localStorage.setItem('token', userData.token)
      console.log('设置新token:', userData.token.substring(0, 20) + '...')
    } else {
      console.log('登录响应中没有token，保持现有token状态')
    }

    // 更新用户信息
    userInfo.value.username = userData.username || userInfo.value.username
    userInfo.value.avatar = userData.avatar || userInfo.value.avatar
    userInfo.value.bio = userData.bio || userInfo.value.bio
    userInfo.value.phone = userData.phone || userInfo.value.phone
    userInfo.value.registerTime = userData.registerTime || userInfo.value.registerTime
    userInfo.value.role = userData.role || userInfo.value.role
    userInfo.value.status = userData.status || userInfo.value.status

    isLoggedIn.value = true
    saveState()
  }

  /**
   * 退出登录
   */
  async function logout() {
    try {
      // 判断当前是否在管理员页面
      const isAdmin = window.location.pathname.includes('/admin');

      // 调用后端登出API（如果需要）
      try {
        await userApi.logout();
      } catch (error) {
        console.warn('调用后端登出接口失败:', error);
      }

      // 清除本地存储的用户信息和token
      localStorage.removeItem('token');
      localStorage.removeItem('userState');

      // 重置userInfo状态
      userInfo.value = {
        id: null,
        username: '',
        avatar: '',
        role: '',
        token: '',
        bio: '',
        phone: '',
        loginAttempts: 0,
        isLoggedIn: false
      };

      isLoggedIn.value = false;
      token.value = '';

      // 根据当前路径决定重定向到哪个登录页
      if (isAdmin) {
        // 管理员用户退出后跳转到普通用户登录页
        window.location.href = '/login';
      } else {
        // 普通用户退出后也跳转到普通登录页
        window.location.href = '/login';
      }

      return true;
    } catch (error) {
      console.error('登出失败:', error);
      throw error;
    }
  }

  // 更新用户资料
  function updateProfile(avatar, bio) {
    if (avatar !== undefined) {
      userInfo.value.avatar = avatar
    }
    if (bio !== undefined) {
      userInfo.value.bio = bio
    }
    saveState()
  }

  // 更新用户信息
  function updateUserInfo(info) {
    userInfo.value = {
      ...userInfo.value,
      ...info
    }
    saveState()
  }

  // 初始化时恢复状态
  restoreState()

  return {
    userInfo,
    isLoggedIn,
    token,
    login,
    adminLogin,
    logout,
    setUserInfo,
    updateProfile,
    updateUserInfo,
    isAdmin: () => userInfo.value.role === 'admin',
    isBanned: () => userInfo.value.status === 'banned'
  }
})
