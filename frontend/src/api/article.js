// 文章相关API调用模块

// 导入axios HTTP客户端库
import axios from 'axios';

// 从环境变量获取API基础URL，如果没有则使用默认地址
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// 获取文章列表
export function fetchArticles(params) {
  // GET /api/articles?page=1&search=关键词
  // params是查询参数对象，会被转换为URL查询字符串
  return axios.get(`${BASE_URL}/articles`, { params });
}

// 获取单篇文章详情
export function fetchArticle(id) {
  // GET /api/articles/{id}
  return axios.get(`${BASE_URL}/articles/${id}`);
}

// 更新文章
export function updateArticle(id, data) {
  // PUT /api/articles/{id}  更新文章
  // data是请求体数据，包含要更新的字段
  return axios.put(`${BASE_URL}/articles/${id}`, data);
}

// 新增文章（补充这段）
export function createArticle(data) {
  // POST /api/articles 创建新文章
  return axios.post(`${BASE_URL}/articles`, data);
}

// 删除文章
export function deleteArticle(id) {
  // DELETE /api/articles/{id}  删除文章
  return axios.delete(`${BASE_URL}/articles/${id}`);
}

// 导出文章为PDF
export function exportPdf(id) {
  // GET /api/articles/{id}/export_pdf  导出 PDF，返回二进制流
  // responseType: 'blob'指定返回类型为二进制数据
  return axios.get(`${BASE_URL}/articles/${id}/export_pdf`, {
    responseType: 'blob'
  });
}
