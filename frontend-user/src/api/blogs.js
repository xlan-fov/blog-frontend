import { get, post, put, del } from './request'
import { API_PATHS } from './config'

export default {
  /**
   * 获取博客列表
   * @param {Object} params - 查询参数
   */
  getBlogs(params) {
    return get(API_PATHS.BLOGS.LIST, params)
  },

  /**
   * 获取博客详情
   * @param {Number} id - 博客ID
   */
  getBlogDetail(id) {
    return get(API_PATHS.BLOGS.GET.replace('{id}', id))
  },

  /**
   * 创建博客
   * @param {Object} data - 博客数据
   */
  createBlog(data) {
    return post(API_PATHS.BLOGS.CREATE, data)
  },

  /**
   * 更新博客
   * @param {Number} id - 博客ID
   * @param {Object} data - 博客数据
   */
  updateBlog(id, data) {
    return put(API_PATHS.BLOGS.UPDATE.replace('{id}', id), data)
  },

  /**
   * 删除博客
   * @param {Number} id - 博客ID
   */
  deleteBlog(id) {
    return del(API_PATHS.BLOGS.DELETE.replace('{id}', id))
  },

  /**
   * 发布博客
   * @param {Number} id - 博客ID
   */
  publishBlog(id, data) {
    return put(API_PATHS.BLOGS.PUBLISH.replace('{id}', id), {
      ...data,
      status: 'published'
    })
  },

  /**
   * 撤回博客
   * @param {Number} id - 博客ID
   */
  withdrawBlog(id, data) {
    return post(API_PATHS.BLOGS.USER_WITHDRAW.replace('{id}', id), data)
  },
  
  /**
   * 获取草稿
   * @param {Number} userId - 用户ID
   */
  getDraft(userId) {
    return get(API_PATHS.BLOGS.GET_DRAFT, { userId })
  },
  
  /**
   * 保存博客内容（草稿）
   * @param {Object} data - 博客数据
   */
  saveContent(data) {
    return post(API_PATHS.BLOGS.SAVE_CONTENT, data)
  }
}
