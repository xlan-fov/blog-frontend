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
    // 确保config和url存在
    if (!config || !config.url) {
      console.error('请求配置或URL未定义:', config);
      return Promise.reject(new Error('请求配置错误'));
    }

    console.log(`DEBUG: 请求URL: ${config.url}, 方法: ${config.method}, 数据:`, config.data || config.params);

    // 对于验证码等公开接口，不需要token验证
    const publicPaths = ['/api/users/captcha', '/api/users/registerByname', '/api/users/loginByname']
    const isPublicPath = publicPaths.some(path => config.url && config.url.includes(path))

    if (!isPublicPath) {
      // 从localStorage获取token并添加到请求头
      const token = localStorage.getItem('token')

      if (token) {
        // 验证token是否是有效的JWT格式
        const tokenParts = token.split('.');
        if (tokenParts.length === 3) {
          console.log('Token验证通过: 包含3个部分');
          // 直接发送原始token，不添加任何前缀
          config.headers['Authorization'] = token;
          console.log('发送请求到:', config.url, '使用token:', token.substring(0, 20) + '...');
        } else {
          console.warn('警告: Token格式不正确!', token);
          // 清除无效token
          localStorage.removeItem('token');
          localStorage.removeItem('userState');
        }

        console.log('Authorization头的值:', config.headers['Authorization']);
        config.withCredentials = true;
      } else {
        console.log('未找到token - 这可能会导致401错误');
        // 对于需要认证的接口，可以在这里重定向到登录页
      }
    } else {
      console.log('公开接口，跳过token验证:', config.url);
    }

    return config;
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 添加token过期处理
service.interceptors.response.use(
  response => {
    console.log(`DEBUG: 响应URL: ${response.config.url}, 状态: ${response.status}, 数据:`, response.data);

    // 处理未登录的情况
    if (response.data?.code === 500 && response.data?.msg === '用户未登录，请先登录') {
      console.warn('用户未登录，清理登录状态并跳转到登录页');
      // 清理本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('userState');
      // 跳转到登录页面
      window.location.href = '/login';
      return Promise.reject(new Error('用户未登录'));
    }

    // 标准化响应格式，将code=0转换为code=200，保持前端一致性
    if (response.data && response.data.code === 0 && response.data.msg === 'success') {
      return {
        ...response.data,
        code: 200
      };
    }

    return response.data;
  },
  error => {
    console.error('❌ 响应错误:', {
      url: error.config?.url,
      status: error.response?.status,
      data: error.response?.data,
      message: error.message
    });

    // 处理401未授权错误（token过期或无效）
    if (error.response?.status === 401) {
      console.warn('Token已过期或无效，清理登录状态');
      // 清理本地存储
      localStorage.removeItem('token');
      localStorage.removeItem('userState');
      // 跳转到登录页面
      window.location.href = '/login';
    }

    // 增强错误处理，提供更具体的错误信息
    if (error.response?.status === 500) {
      console.error('服务器内部错误，请检查后端日志');
      ElMessage.error('服务器处理请求失败，请稍后再试');
    } else if (error.response?.status === 400) {
      console.error('请求参数错误:', error.response.data);
      const errorMsg = error.response.data?.msg || '请求参数错误';
      ElMessage.error(errorMsg);
    } else if (error.response?.status === 404) {
      ElMessage.error('请求的资源不存在');
    } else {
      ElMessage.error('请求失败，请稍后再试');
    }

    return Promise.reject(error);
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