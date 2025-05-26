import { get, post, put, del } from './request'
import { API_PATHS } from './config'

export default {
  /**
   * 获取博客列表
   * @param {Object} params - 分页和筛选参数
   */
  async getBlogs(params) {
    try {
      const response = await get(API_PATHS.BLOGS.LIST, params)

      if (response?.code === 200 && response.data) {
        if (response.data.records) {
          return {
            code: 200,
            data: {
              records: Array.isArray(response.data.records) ? response.data.records : [],
              total: response.data.total || 0,
              current: response.data.current || 1,
              size: response.data.size || 10
            }
          }
        } else if (Array.isArray(response.data)) {
          return {
            code: 200,
            data: response.data
          }
        }
      }

      return {
        code: response?.code || 500,
        message: response?.message || '获取博客列表失败',
        data: []
      }
    } catch (error) {
      console.error('获取博客列表失败:', error)
      return {
        code: 500,
        message: error.message || '获取博客列表失败',
        data: []
      }
    }
  },

  /**
   * 获取单个博客
   * @param {number|string} id - 博客ID
   */
  async getBlogById(id) {
    try {
      const response = await get(`${API_PATHS.BLOGS.DETAIL}/${id}`)
      return response
    } catch (error) {
      console.error('获取博客详情失败:', error)
      return {
        code: 500,
        message: error.message || '获取博客详情失败'
      }
    }
  },

  /**
   * 创建博客
   * @param {Object} data - 博客数据
   */
  async createBlog(data) {
    try {
      const response = await post(API_PATHS.BLOGS.CREATE, data)
      return response
    } catch (error) {
      console.error('创建博客失败:', error)
      return {
        code: 500,
        message: error.message || '创建博客失败'
      }
    }
  },

  /**
   * 更新博客
   * @param {number|string} id - 博客ID
   * @param {Object} data - 更新的博客数据
   */
  async updateBlog(id, data) {
    try {
      const response = await put(API_PATHS.BLOGS.UPDATE.replace('{id}', id), data)
      return response
    } catch (error) {
      console.error('更新博客失败:', error)
      return {
        code: 500,
        message: error.message || '更新博客失败'
      }
    }
  },

  /**
   * 删除博客
   * @param {number|string} id - 博客ID
   */
  async deleteBlog(id) {
    try {
      const response = await del(`${API_PATHS.BLOGS.DELETE}/${id}`)
      return response
    } catch (error) {
      console.error('删除博客失败:', error)
      return {
        code: 500,
        message: error.message || '删除博客失败'
      }
    }
  },

  /**
   * 撤回博客
   * @param {number|string} id - 博客ID
   */
  async withdrawBlog(id) {
    try {
      const response = await put(API_PATHS.BLOGS.USER_WITHDRAW.replace('{id}', id))
      return response
    } catch (error) {
      console.error('撤回博客失败:', error)
      return {
        code: 500,
        message: error.message || '撤回博客失败'
      }
    }
  }
}
