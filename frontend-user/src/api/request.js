import axios from 'axios'
import { API_CONFIG } from './config'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
    baseURL: API_CONFIG.BASE_URL,
    timeout: API_CONFIG.TIMEOUT,
    headers: API_CONFIG.HEADERS
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        // 这里可以添加token等认证信息
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        // 这里可以根据后端的响应结构统一处理
        if (res.code !== 0) {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        return res
    },
    error => {
        console.error('响应错误:', error)
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default request 