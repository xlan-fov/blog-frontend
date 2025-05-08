// 管理员相关API调用模块

// 导入axios HTTP客户端库
import axios from 'axios'

// 从环境变量获取API基础URL，如果没有则使用默认地址
const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 获取用户列表
export function getUserList(params) {
  // GET /admin/users
  return axios.get(`${BASE}/admin/users`, { params })
}

// 创建用户
export function createUser(data) {
  // POST /admin/users
  return axios.post(`${BASE}/admin/users`, data)
}

// 踢出用户
export function kickUser(id) {
  // POST /admin/users/{id}/kick
  return axios.post(`${BASE}/admin/users/${id}/kick`)
}

// 封禁用户
export function banUser(id) {
  // POST /admin/users/{id}/ban
  return axios.post(`${BASE}/admin/users/${id}/ban`)
}

// 解封用户
export function unbanUser(id) {
  // POST /admin/users/{id}/unban
  return axios.post(`${BASE}/admin/users/${id}/unban`)
}

// 获取异常登录记录
export function getAbnormalLogins(params) {
  // GET /admin/logins/abnormal
  return axios.get(`${BASE}/admin/logins/abnormal`, { params })
}

// 获取不良内容
export function getBadContent(params) {
  // GET /admin/articles/bad
  return axios.get(`${BASE}/admin/articles/bad`, { params })
}
