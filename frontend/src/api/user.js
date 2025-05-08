// 用户相关API调用模块

// 导入axios HTTP客户端库
import axios from 'axios'

// 从环境变量获取API基础URL，如果没有则使用默认地址
const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 检查用户名是否可用
export function checkUsername(name) {
  // GET /users/check?username=
  return axios.get(`${BASE}/users/check`, { params: { username: name } })
}

// 用户注册
export function register(data) {
  // POST /users/register
  return axios.post(`${BASE}/users/register`, data)
}

// 用户登录
export function login(data) {
  // POST /users/login
  return axios.post(`${BASE}/users/login`, data)
}

// 用户登出
export function logout() {
  // POST /users/logout
  return axios.post(`${BASE}/users/logout`)
}

// 获取用户个人资料
export function getProfile() {
  // GET /users/profile
  return axios.get(`${BASE}/users/profile`)
}

// 更新用户个人资料
export function updateProfile(data) {
  // PUT /users/profile
  return axios.put(`${BASE}/users/profile`, data)
}

// 修改密码
export function changePassword(payload) {
  // PUT /users/password
  return axios.put(`${BASE}/users/password`, payload)
}
