import axios from 'axios'
import { API_CONFIG } from './config'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: API_CONFIG.HEADERS
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 记录请求
    console.log('⬆️ 发送请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      params: config.params
    })
    
    // 从localStorage获取token并添加到请求头
    const token = localStorage.getItem('token')
    if (token) {
      // 尝试不同的认证头格式
      config.headers['Authorization'] = `Bearer ${token}`
      // 有些后端可能期望不同的格式
      config.headers['token'] = token
      // 确保携带cookies
      config.withCredentials = true
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 记录响应
    console.log('⬇️ 接收响应:', {
      url: response.config.url,
      status: response.status,
      data: response.data
    })
    
    const res = response.data
    // 根据后端返回的状态码进行处理
    if (res.code && res.code !== 200) {
      // 401: 未登录或token过期，改为不立即强制跳转
      if (res.code === 401) {
        console.warn('认证失败:', res.message)
        // 显示友好的提示而不是强制跳转
        ElMessage({
          message: '登录已过期，请重新登录',
          type: 'warning',
          duration: 5000,
          showClose: true,
          onClose: () => {
            // 清除登录状态
            localStorage.removeItem('userState')
            localStorage.removeItem('token')
            // 仅当用户不在登录页时跳转
            if (window.location.pathname !== '/login') {
              window.location.href = '/login'
            }
          }
        })
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('❌ 响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data
    })
    
    console.error('Response error:', error)
    // 处理网络错误
    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          localStorage.removeItem('userState')
          localStorage.removeItem('token')
          setTimeout(() => {
            window.location.href = '/login'
          }, 1500)
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `请求错误 ${error.response.status}`
      }
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

// 封装GET请求
export function get(url, params) {
  return service({
    url,
    method: 'get',
    params
  })
}

// 封装POST请求
export function post(url, data) {
  return service({
    url,
    method: 'post',
    data
  })
}

// 封装PUT请求
export function put(url, data) {
  return service({
    url,
    method: 'put',
    data
  })
}

// 封装DELETE请求
export function del(url, data) {
  return service({
    url,
    method: 'delete',
    data
  })
}

// 导出默认实例
export default service