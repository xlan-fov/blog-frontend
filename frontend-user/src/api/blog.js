import { get, post, put, del } from './request'
import { API_PATHS } from './config'

export default {
  /**
   * 获取博客列表
   * @param {Object} params - 分页和筛选参数
   */
  getBlogs(params) {
    return get(API_PATHS.BLOGS.LIST, params)
  },

  /**
   * 根据ID获取博客详情
   * @param {String} id - 博客ID
   */
  getBlogById(id) {
    return get(API_PATHS.BLOGS.GET.replace('{id}', id))
  },

  /**
   * 创建新博客
   * @param {Object} data - 博客数据
   */
  createBlog(data) {
    return post(API_PATHS.BLOGS.CREATE, data)
  },

  /**
   * 更新博客
   * @param {String} id - 博客ID
   * @param {Object} data - 博客更新数据
   */
  updateBlog(id, data) {
    return put(API_PATHS.BLOGS.UPDATE.replace('{id}', id), data)
  },

  /**
   * 删除博客
   * @param {String} id - 博客ID
   */
  deleteBlog(id) {
    return del(API_PATHS.BLOGS.DELETE.replace('{id}', id))
  },

  /**
   * 发布博客
   * @param {String} id - 博客ID
   */
  publishBlog(id) {
    return post(API_PATHS.BLOGS.PUBLISH.replace('{id}', id))
  },

  /**
   * 撤回博客
   * @param {String} id - 博客ID
   */
  withdrawBlog(id) {
    return post(API_PATHS.BLOGS.WITHDRAW.replace('{id}', id))
  },

  /**
   * 搜索博客
   * @param {Object} params - 搜索参数
   */
  searchBlogs(params) {
    return get(API_PATHS.BLOGS.SEARCH, params)
  }
}
