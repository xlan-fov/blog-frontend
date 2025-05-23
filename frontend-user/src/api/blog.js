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
      
      // 确保返回正确的数据格式
      if (response && response.data) {
        // 如果后端返回的是分页数据
        if (response.data.records) {
          return {
            data: {
              records: Array.isArray(response.data.records) ? response.data.records : [],
              total: response.data.total || 0,
              current: response.data.current || 1,
              size: response.data.size || 10
            }
          }
        }
        // 如果后端直接返回数组
        else if (Array.isArray(response.data)) {
          return {
            data: response.data
          }
        }
      }
      
      // 默认返回空数组
      return {
        data: []
      }
    } catch (error) {
      console.error('获取博客列表失败:', error)
      return {
        data: []
      }
    }
  },

  /**
   * 获取博客详情
   * @param {string|number} id - 博客ID
   */
  async getBlogById(id) {
    try {
      const response = await get(API_PATHS.BLOGS.GET.replace('{id}', id))
      return response
    } catch (error) {
      console.error('获取博客详情失败:', error)
      throw error
    }
  },

  /**
   * 创建博客
   * @param {Object} blogData - 博客数据
   */
  async createBlog(blogData) {
    try {
      const response = await post(API_PATHS.BLOGS.CREATE, blogData)
      return response
    } catch (error) {
      console.error('创建博客失败:', error)
      throw error
    }
  },

  /**
   * 更新博客
   * @param {string|number} id - 博客ID
   * @param {Object} blogData - 博客数据
   */
  async updateBlog(id, blogData) {
    try {
      const response = await put(API_PATHS.BLOGS.UPDATE, {
        id,
        ...blogData
      })
      return response
    } catch (error) {
      console.error('更新博客失败:', error)
      throw error
    }
  },

  /**
   * 删除博客
   * @param {string|number} id - 博客ID
   */
  async deleteBlog(id) {
    try {
      const response = await del(API_PATHS.BLOGS.DELETE.replace('{id}', id))
      return response
    } catch (error) {
      console.error('删除博客失败:', error)
      throw error
    }
  },

  /**
   * 发布博客 - 单独的接口方法
   * @param {Object} blogData - 博客数据，包含status='published'
   */
  async publishBlog(blogData) {
    try {
      console.log('API层 - 发布博客请求数据:', blogData)
      // 确保状态为已发布
      const data = {
        ...blogData,
        status: 'published' 
      }
      
      // 对于新博客使用创建接口
      if (!data.id) {
        return await post(API_PATHS.BLOGS.CREATE, data)
      } 
      // 对于现有博客使用更新接口
      else {
        return await put(API_PATHS.BLOGS.UPDATE, data)
      }
    } catch (error) {
      console.error('API层 - 发布博客失败:', error)
      throw error
    }
  },

  /**
   * 撤回博客
   * @param {string|number} id - 博客ID
   */
  async withdrawBlog(id) {
    try {
      const response = await post(API_PATHS.BLOGS.WITHDRAW.replace('{id}', id), {})
      return response
    } catch (error) {
      console.error('撤回博客失败:', error)
      throw error
    }
  }
}
